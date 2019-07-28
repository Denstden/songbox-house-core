package songbox.house.service.search.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songbox.house.domain.dto.request.SearchRequestDto;
import songbox.house.domain.entity.SearchHistory;
import songbox.house.domain.entity.Track;
import songbox.house.service.TrackService;
import songbox.house.service.search.SearchDownloadServiceFacade;
import songbox.house.service.search.SearchHistoryService;
import songbox.house.service.search.vk.VkSearchDownloadService;

import java.util.Optional;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
@AllArgsConstructor
@Slf4j
public class SearchDownloadServiceFacadeImpl implements SearchDownloadServiceFacade {

    SearchHistoryService searchHistoryService;
    VkSearchDownloadService vkSearchDownloadService;
    TrackService trackService;

    @Override
    public Optional<Track> searchAndDownload(final SearchRequestDto searchRequest) {
        final String authors = searchRequest.getArtists().trim();
        final String title = searchRequest.getTitle().trim();

        final SearchHistory searchHistory = createSearchHistory(authors, title);

        return ofNullable(trackService.findByArtistAndTitle(authors, title))
                .map(fromDB -> {
                    log.debug("Found track in db, not perform searching.");
                    searchHistoryService.saveSuccess(searchHistory, fromDB, true);
                    return of(fromDB);
                })
                .orElseGet(() -> vkSearchDownloadService.searchAndDownload(authors, title, searchRequest.getGenres(), searchRequest.getCollectionId(), searchHistory));
    }

    @Override
    @Async
    public void searchAndDownloadAsync(final SearchRequestDto searchRequest) {
        final String authors = searchRequest.getArtists().trim();
        final String title = searchRequest.getTitle().trim();

        final SearchHistory searchHistory = createSearchHistory(authors, title);

        final Track fromDb = trackService.findByArtistAndTitle(authors, title);
        if (fromDb != null) {
            log.debug("Found track in db, not perform searching.");
            searchHistoryService.saveSuccess(searchHistory, fromDb, true);
        } else {
            vkSearchDownloadService.searchAndDownloadAsync(authors, title, searchRequest.getGenres(), searchRequest.getCollectionId(), searchHistory, searchRequest.isOnly320());
        }
    }

    private SearchHistory createSearchHistory(final String authors, final String title) {
        final SearchHistory searchHistory = new SearchHistory();
        searchHistory.setArtists(authors);
        searchHistory.setTitle(title);
        return searchHistory;
    }
}
