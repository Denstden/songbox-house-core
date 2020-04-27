package songbox.house.service.search.impl;

import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import songbox.house.domain.dto.request.SearchQueryDto;
import songbox.house.domain.dto.response.SearchResultDto;
import songbox.house.domain.dto.response.TrackMetadataDto;
import songbox.house.service.DiscogsWebsiteService;
import songbox.house.service.search.SearchService;
import songbox.house.service.search.SearchServiceFacade;
import songbox.house.util.ArtistsTitle;
import songbox.house.util.ExecutorUtil;
import songbox.house.util.compare.TrackMetadataComparator;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;
import static java.lang.System.currentTimeMillis;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PRIVATE;

@Service
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Slf4j
public class SearchServiceFacadeImpl implements SearchServiceFacade {

    List<SearchService> searchServices;
    DiscogsWebsiteService discogsWebsiteService;
    ExecutorService searchExecutorService;
    Integer searchServiceTimeoutMs;

    @Autowired
    public SearchServiceFacadeImpl(List<SearchService> searchServices, DiscogsWebsiteService discogsWebsiteService,
            @Value("${songbox.house.search.threads:2}") Integer searchThreads,
            @Value("${songbox.house.search.service.timeout.ms:10000}") Integer searchServiceTimeoutMs) {
        this.searchServices = searchServices;
        this.discogsWebsiteService = discogsWebsiteService;
        this.searchExecutorService = ExecutorUtil.createExecutorService(searchThreads);
        this.searchServiceTimeoutMs = searchServiceTimeoutMs;
    }

    @Override
    public List<TrackMetadataDto> search(SearchQueryDto query) {
        return doSearch(query, false);
    }

    private List<TrackMetadataDto> doSearch(SearchQueryDto query, boolean fast) {
        log.info("Starting search for {}", query);
        long searchStart = currentTimeMillis();

        CompletableFuture<Optional<String>> artworkFuture = null;
        if (query.isFetchArtwork()) {
            artworkFuture = supplyAsync(() -> discogsWebsiteService.searchArtwork(query.getQuery()));
        }
        final List<TrackMetadataDto> songs = searchSongs(query, fast);

        ofNullable(artworkFuture)
                .flatMap(CompletableFuture::join)
                .ifPresent(artwork -> songs.forEach(song -> song.setThumbnail(artwork)));

        ArtistsTitle artistTitle = ArtistsTitle.parse(query.getQuery());
        sort(songs, artistTitle);

        log.info("Search finished {}ms, found {} items", currentTimeMillis() - searchStart, songs.size());

        //TODO change comparator to no need reverse
        return reverse(songs);
    }

    private List<TrackMetadataDto> searchSongs(SearchQueryDto query, boolean fast) {
        CompletionService<SearchResultDto> completionService = new ExecutorCompletionService<>(searchExecutorService);
        searchServices.forEach(service -> completionService.submit(() -> ((fast)
                ? service.searchFast(query)
                : service.search(query))));
        List<TrackMetadataDto> songs = newArrayList();
        for (int i = 0; i < searchServices.size(); i++) {
            try {
                songs.addAll(completionService.take().get(searchServiceTimeoutMs, MILLISECONDS).getSongs());
            } catch (Exception e) {
                log.error("Can't take the search service result", e);
            }
        }
        return songs;
    }

    @Override
    public List<TrackMetadataDto> searchFast(SearchQueryDto query) {
        return doSearch(query, true);
    }


    private void sort(List<TrackMetadataDto> songs, ArtistsTitle artistTitle) {
        final TrackMetadataComparator comparator = new TrackMetadataComparator(artistTitle, 70);
        songs.sort(comparator);
    }

}
