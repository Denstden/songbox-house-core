package songbox.house.service.search.impl;

import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import songbox.house.domain.dto.response.TrackMetadataDto;
import songbox.house.domain.entity.SearchHistory;
import songbox.house.service.search.SearchHistoryService;
import songbox.house.service.search.SearchQueryDto;
import songbox.house.service.search.SearchService;
import songbox.house.service.search.SearchServiceFacade;
import songbox.house.service.search.vk.VkSearchService;
import songbox.house.service.search.youtube.YoutubeSearchService;
import songbox.house.util.Pair;
import songbox.house.util.compare.SearchResultComparator;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static lombok.AccessLevel.PRIVATE;
import static songbox.house.util.ArtistTitleUtil.extractArtistTitle;
import static songbox.house.util.Constants.PERFORMANCE_MARKER;

@Service
@FieldDefaults(makeFinal = true, level = PRIVATE)
@AllArgsConstructor
@Slf4j
public class SearchServiceFacadeImpl implements SearchServiceFacade {

    List<SearchService> searchServices;
    SearchHistoryService searchHistoryService;

    @Autowired
    public SearchServiceFacadeImpl(VkSearchService vkSearchService,
            YoutubeSearchService youtubeSearchService,
            SearchHistoryService searchHistoryService,
            @Value("${songbox.house.youtube.search.enabled}") Boolean youtubeSearchEnabled) {
        this.searchHistoryService = searchHistoryService;
        this.searchServices = youtubeSearchEnabled ? asList(vkSearchService, youtubeSearchService) : singletonList(vkSearchService);
    }

    @Override
    public List<TrackMetadataDto> search(SearchQueryDto query) {
        log.info("Starting search for {}", query);
        long searchStart = currentTimeMillis();

        List<TrackMetadataDto> songs = newArrayList();

        for (SearchService searchService : searchServices) {
            try {
                songs.addAll(searchService.search(query));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        Pair<String, String> artistTitle = extractArtistTitle(query.getQuery());
        sort(songs, artistTitle);

        //TODO change comparator to no need reverse
        songs = reverse(songs);

        saveSearchHistory(songs, artistTitle);
        log.info(PERFORMANCE_MARKER, "Search VK+Youtube finished {}ms", currentTimeMillis() - searchStart);

        return songs;
    }

    private void saveSearchHistory(List<TrackMetadataDto> songs, Pair<String, String> artistTitle) {
        SearchHistory searchHistory = createSearchHistory(artistTitle);
        if (!songs.isEmpty()) {
            String uriList = songs.stream().findFirst().get().getUri();
            searchHistoryService.saveSuccess(searchHistory, uriList);
        } else {
            searchHistoryService.saveFail(searchHistory);
        }
    }

    private SearchHistory createSearchHistory(Pair<String, String> artistTitle) {
        SearchHistory searchHistory = new SearchHistory();
        searchHistory.setArtists(artistTitle.getLeft());
        searchHistory.setTitle(artistTitle.getRight());
        return searchHistory;
    }

    private void sort(List<TrackMetadataDto> songs, Pair<String, String> artistTitle) {
        final SearchResultComparator comparator = new SearchResultComparator(artistTitle.getRight(), artistTitle.getLeft());
        songs.sort(comparator);
    }

}
