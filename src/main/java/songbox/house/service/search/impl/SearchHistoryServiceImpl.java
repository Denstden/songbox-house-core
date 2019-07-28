package songbox.house.service.search.impl;

import com.google.common.collect.Iterables;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songbox.house.domain.entity.SearchHistory;
import songbox.house.domain.entity.Track;
import songbox.house.repository.SearchHistoryRepository;
import songbox.house.service.search.SearchHistoryService;

import static songbox.house.domain.SearchStatus.FAIL;
import static songbox.house.domain.SearchStatus.SUCCESS;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Transactional
public class SearchHistoryServiceImpl implements SearchHistoryService {

    SearchHistoryRepository repository;

    @Override
    public SearchHistory save(final SearchHistory searchHistory) {
        return repository.save(searchHistory);
    }

    @Override
    public SearchHistory saveSuccess(final SearchHistory searchHistory, final Track track, final boolean existsInDb) {
        searchHistory.setSearchStatus(SUCCESS);
        searchHistory.setTrack(track);
        searchHistory.setExistsInDb(existsInDb);
        return repository.save(searchHistory);
    }

    @Override
    public SearchHistory saveSuccess(SearchHistory searchHistory, String uri) {
        searchHistory.setSearchStatus(SUCCESS);
        searchHistory.setUri(uri);
        return repository.save(searchHistory);
    }

    @Override
    public SearchHistory saveFail(final SearchHistory searchHistory) {
        searchHistory.setSearchStatus(FAIL);
        return repository.save(searchHistory);
    }

    @Override
    public Integer clearSuccessEvents() {
        final Iterable<SearchHistory> successEvents = repository.findBySearchStatus(SUCCESS);
        final int size = Iterables.size(successEvents);

        repository.deleteAll(successEvents);

        return size;
    }
}
