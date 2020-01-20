package songbox.house.service.search.impl;

import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import songbox.house.domain.dto.request.SearchQueryDto;
import songbox.house.domain.dto.response.TrackMetadataDto;
import songbox.house.service.search.SearchService;
import songbox.house.service.search.SearchServiceFacade;
import songbox.house.util.Pair;
import songbox.house.util.compare.SearchResultComparator;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;
import static java.lang.System.currentTimeMillis;
import static lombok.AccessLevel.PRIVATE;
import static songbox.house.util.ArtistTitleUtil.extractArtistTitle;
import static songbox.house.util.Constants.PERFORMANCE_MARKER;

@Service
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Slf4j
public class SearchServiceFacadeImpl implements SearchServiceFacade {

    List<SearchService> searchServices;

    @Autowired
    public SearchServiceFacadeImpl(List<SearchService> searchServices) {
        this.searchServices = searchServices;
    }

    @Override
    public List<TrackMetadataDto> search(SearchQueryDto query) {
        log.info("Starting search for {}", query);
        long searchStart = currentTimeMillis();

        List<TrackMetadataDto> songs = newArrayList();
        for (SearchService searchService : searchServices) {
            try {
                songs.addAll(searchService.search(query).getSongs());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        Pair<String, String> artistTitle = extractArtistTitle(query.getQuery());
        sort(songs, artistTitle);

        //TODO change comparator to no need reverse
        songs = reverse(songs);

        log.info(PERFORMANCE_MARKER, "Search VK+Youtube finished {}ms", currentTimeMillis() - searchStart);

        return songs;
    }


    private void sort(List<TrackMetadataDto> songs, Pair<String, String> artistTitle) {
        final SearchResultComparator comparator = new SearchResultComparator(artistTitle.getRight(), artistTitle.getLeft());
        songs.sort(comparator);
    }

}
