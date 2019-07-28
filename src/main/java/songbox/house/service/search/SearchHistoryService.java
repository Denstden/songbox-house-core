package songbox.house.service.search;

import songbox.house.domain.entity.SearchHistory;
import songbox.house.domain.entity.Track;

public interface SearchHistoryService {
    SearchHistory save(SearchHistory searchHistory);

    SearchHistory saveSuccess(SearchHistory searchHistory, Track track, boolean existsInDb);

    SearchHistory saveSuccess(SearchHistory searchHistory, String uri);

    SearchHistory saveFail(SearchHistory searchHistory);

    Integer clearSuccessEvents();
}
