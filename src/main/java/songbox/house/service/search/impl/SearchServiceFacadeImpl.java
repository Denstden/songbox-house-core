package songbox.house.service.search.impl;

import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import songbox.house.domain.dto.request.SearchQueryDto;
import songbox.house.domain.dto.response.TrackMetadataDto;
import songbox.house.service.DiscogsWebsiteService;
import songbox.house.service.search.SearchService;
import songbox.house.service.search.SearchServiceFacade;
import songbox.house.util.Pair;
import songbox.house.util.compare.SearchResultComparator;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;
import static java.lang.System.currentTimeMillis;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static lombok.AccessLevel.PRIVATE;
import static songbox.house.util.ArtistTitleUtil.extractArtistTitle;
import static songbox.house.util.Constants.PERFORMANCE_MARKER;

@Service
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Slf4j
public class SearchServiceFacadeImpl implements SearchServiceFacade {

    List<SearchService> searchServices;
    DiscogsWebsiteService discogsWebsiteService;

    @Autowired
    public SearchServiceFacadeImpl(List<SearchService> searchServices, DiscogsWebsiteService discogsWebsiteService) {
        this.searchServices = searchServices;
        this.discogsWebsiteService = discogsWebsiteService;
    }

    @Override
    public List<TrackMetadataDto> search(SearchQueryDto query) {
        log.info("Starting search for {}", query);
        long searchStart = currentTimeMillis();


        final List<TrackMetadataDto> songs = newArrayList();
        CompletableFuture<List<String>> artworksFuture = null;
        if (query.isFetchArtwork()) {
            artworksFuture = supplyAsync(() -> discogsWebsiteService.searchArtworks(query.getQuery()));
        }
        for (SearchService searchService : searchServices) {
            try {
                songs.addAll(searchService.search(query).getSongs());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        ofNullable(artworksFuture)
                .map(CompletableFuture::join)
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .ifPresent(artwork -> songs.forEach(song -> song.setThumbnail(artwork)));

        Pair<String, String> artistTitle = extractArtistTitle(query.getQuery());
        sort(songs, artistTitle);

        log.info(PERFORMANCE_MARKER, "Search VK+Youtube finished {}ms", currentTimeMillis() - searchStart);

        //TODO change comparator to no need reverse
        return reverse(songs);
    }


    private void sort(List<TrackMetadataDto> songs, Pair<String, String> artistTitle) {
        final SearchResultComparator comparator = new SearchResultComparator(artistTitle.getRight(), artistTitle.getLeft());
        songs.sort(comparator);
    }

}
