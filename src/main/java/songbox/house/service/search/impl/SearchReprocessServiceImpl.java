package songbox.house.service.search.impl;

import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songbox.house.domain.dto.SearchReprocessResultDto;
import songbox.house.domain.dto.request.SearchQueryDto;
import songbox.house.domain.dto.response.TrackMetadataDto;
import songbox.house.domain.entity.SearchReprocess;
import songbox.house.event.SearchReprocessFoundEvent;
import songbox.house.repository.SearchReprocessRepository;
import songbox.house.repository.SearchReprocessResultRepository;
import songbox.house.service.search.SearchReprocessService;
import songbox.house.service.search.SearchServiceFacade;
import songbox.house.service.search.TrackDownloadService;
import songbox.house.util.ArtistsTitle;
import songbox.house.util.Pair;
import songbox.house.util.compare.TrackMetadataComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.join;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;
import static songbox.house.domain.entity.SearchReprocessStatus.DOWNLOADED;
import static songbox.house.domain.entity.SearchReprocessStatus.FOUND;
import static songbox.house.domain.entity.SearchReprocessStatus.NOT_FOUND;

@Service
@Slf4j
@FieldDefaults(makeFinal = true, level = PRIVATE)
@AllArgsConstructor
public class SearchReprocessServiceImpl implements SearchReprocessService {

    SearchReprocessRepository repository;
    SearchReprocessResultRepository reprocessResultRepository;
    SearchServiceFacade searchServiceFacade;
    TrackDownloadService downloadService;
    ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public SearchReprocess createIfNotExists(String searchQuery, Long collectionId, Set<String> genres, Long userId) {
        return repository.findByUserIdAndSearchQuery(userId, searchQuery)
                .map(searchReprocess -> {
                    if (DOWNLOADED == searchReprocess.getStatus()) {
                        repository.setNotFoundById(searchReprocess.getId());
                    }
                    return searchReprocess;
                })
                .orElseGet(() -> create(searchQuery, collectionId, genres, userId));
    }

    @Override
    public Page<SearchReprocess> availableForSearch(Long userId, Pageable pageable) {
        return findNotFound(userId, pageable);
    }

    @Override
    @Transactional
    public Page<SearchReprocess> availableForDownloading(Long userId, Pageable pageable) {
        checkConsistency(userId);
        return repository.findByUserIdAndStatus(userId, FOUND, pageable);
    }

    @Override
    public Page<SearchReprocess> downloaded(Long userId, Pageable pageable) {
        return repository.findByUserIdAndStatus(userId, DOWNLOADED, pageable);
    }

    @Override
    @Transactional
    public void download(Long userId, Set<Long> searchReprocessIds) {
        log.info("Starting downloading search reprocess ids {} for user {}", searchReprocessIds, userId);
        Map<Long, SearchReprocessResultDto> readyForDownloading =
                reprocessResultRepository.get(userId, searchReprocessIds);

        int downloaded = download(userId, readyForDownloading);
        log.info("Downloaded {} tracks for user {}", downloaded, userId);
    }

    @Override
    @Transactional
    public void downloadAll(Long userId) {
        log.info("Starting downloading all search reprocess ids for user {}", userId);
        Map<Long, SearchReprocessResultDto> readyForDownloading = reprocessResultRepository.available(userId);

        int downloaded = download(userId, readyForDownloading);
        log.info("Downloaded {} tracks for user {}", downloaded, userId);
    }

    @Override
    @Transactional
    @Scheduled(cron = "${songbox.house.reprocess.cron:0 0 0 * * *}")
    public void reprocessAllUsers() {
        log.info("Starting reprocessing search requests for all users");
        repository.findAllUsersForReprocess(NOT_FOUND).forEach(this::reprocess);
        log.info("Finished reprocessing search requests for all users");
    }

    @Override
    @Transactional
    public void reprocess(Long userId) {
        log.info("Starting reprocessing search requests for user {}", userId);
        checkConsistency(userId);

        Map<Long, SearchReprocessResultDto> reprocessResult = new HashMap<>();

        int pageNumber = 0;
        Page<SearchReprocess> batch;
        do {
            //TODO config
            Pageable pageable = PageRequest.of(pageNumber, 20);
            batch = repository.findByUserIdAndStatus(userId, NOT_FOUND, pageable);
            reprocessResult.putAll(reprocessBatch(batch.getContent()));
            repository.incrementRetryCount(batch.stream().map(SearchReprocess::getId).collect(toSet()));
            pageNumber++;
        } while (batch.hasNext());

        if (!reprocessResult.isEmpty()) {
            reprocessResultRepository.save(userId, reprocessResult);
            repository.setFound(new Date(), new ArrayList<>(reprocessResult.keySet()));
            applicationEventPublisher.publishEvent(new SearchReprocessFoundEvent(this, userId, reprocessResult));
            //TODO check user property -> if auto download after reprocessing -> downloadAll(userId)
        }
        log.info("Finished reprocessing search requests for user {}, found {}", userId, reprocessResult.size());
    }

    private Optional<Long> downloadOne(Long searchReprocessId, SearchReprocessResultDto resultDto) {
        return downloadService.download(resultDto.getTrackMetadata(), resultDto.getCollectionId(),
                resultDto.getOwnerId(), resultDto.getGenres())
                .map(t -> searchReprocessId);
    }

    private int download(Long userId, Map<Long, SearchReprocessResultDto> readyForDownloading) {
        final Set<Long> downloadedReprocessIds = readyForDownloading.entrySet().stream()
                .map(entry -> downloadOne(entry.getKey(), entry.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());

        if (!downloadedReprocessIds.isEmpty()) {
            repository.setDownloaded(new Date(), downloadedReprocessIds);
            reprocessResultRepository.remove(userId, downloadedReprocessIds);
        }

        return downloadedReprocessIds.size();
    }

    private void checkConsistency(Long userId) {
        Set<Long> availableReprocessIds = reprocessResultRepository.available(userId).keySet();
        if (!availableReprocessIds.isEmpty()) {
            repository.setNotFound(availableReprocessIds);
        } else {
            repository.setNotFound(userId);
        }
    }

    private Map<Long, SearchReprocessResultDto> reprocessBatch(Collection<SearchReprocess> batch) {
        //TODO parallel if need
        return batch.stream()
                .map(this::reprocessOneSearch)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toMap(Pair::getLeft, Pair::getRight));
    }

    private Optional<Pair<Long, SearchReprocessResultDto>> reprocessOneSearch(SearchReprocess searchReprocess) {
        String searchQuery = searchReprocess.getSearchQuery();
        SearchQueryDto searchQueryDto = new SearchQueryDto(searchQuery);
        //TODO config
        TrackMetadataComparator comparator = new TrackMetadataComparator(ArtistsTitle.parse(searchQuery), 85);

        List<TrackMetadataDto> searchResult = searchServiceFacade.search(searchQueryDto).stream()
                .sorted(comparator.reversed())
                .collect(toList());

        if (searchResult.isEmpty()) {
            return Optional.empty();
        } else {
            log.debug("Reprocess search result sorted {}, saving 1st element as result", searchResult);
            final TrackMetadataDto foundTrackMetadata = searchResult.get(0);
            SearchReprocessResultDto resultDto = new SearchReprocessResultDto();
            resultDto.setTrackMetadata(foundTrackMetadata);
            resultDto.setCollectionId(searchReprocess.getCollectionId());
            resultDto.setOwnerId(searchReprocess.getUserId());
            final Set<String> genres = Stream.of(searchReprocess.getGenres().split(","))
                    .filter(StringUtils::isNotBlank)
                    .collect(toSet());
            resultDto.setGenres(genres);
            return Optional.of(Pair.of(searchReprocess.getId(), resultDto));
        }
    }

    private Page<SearchReprocess> findNotFound(Long userId, Pageable pageable) {
        return repository.findByUserIdAndStatus(userId, NOT_FOUND, pageable);
    }

    private SearchReprocess create(String searchQuery, Long collectionId, Set<String> genres, Long userId) {
        SearchReprocess searchReprocess = new SearchReprocess();
        searchReprocess.setSearchQuery(searchQuery);
        searchReprocess.setCollectionId(collectionId);
        searchReprocess.setUserId(userId);
        searchReprocess.setGenres(join(",", genres));
        return repository.save(searchReprocess);
    }

}
