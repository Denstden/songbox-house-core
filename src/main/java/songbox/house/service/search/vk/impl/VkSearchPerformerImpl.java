package songbox.house.service.search.vk.impl;

import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import songbox.house.client.VkClient;
import songbox.house.domain.dto.response.vk.VkSearchResponseDto;
import songbox.house.service.search.SearchQueryDto;
import songbox.house.service.search.vk.VkSearchPerformer;
import songbox.house.service.search.vk.VkSearchResultAnalyzer;
import songbox.house.util.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

import static com.google.api.client.util.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.util.CollectionUtils.isEmpty;
import static songbox.house.util.ExecutorUtil.createExecutorService;
import static songbox.house.util.JsonUtils.responseToObject;
import static songbox.house.util.JsonUtils.searchFromNewsFeedResponseToObject;
import static songbox.house.util.RetryUtil.getOptionalWithRetries;

@Slf4j
@Service
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class VkSearchPerformerImpl implements VkSearchPerformer {

    Configuration configuration;
    VkClient vkClient;
    VkSearchResultAnalyzer vkSearchResultAnalyzer;
    ExecutorService searchExecutorService;

    Integer searchFromMusicRetries;

    public VkSearchPerformerImpl(Configuration configuration, VkClient vkClient,
            VkSearchResultAnalyzer vkSearchResultAnalyzer,
            @Value("${songbox.house.vk.search.music_retries}") Integer searchFromMusicRetries,
            @Value("${songbox.house.vk.search.threads}") Integer searchThreads) {
        this.configuration = configuration;
        this.vkClient = vkClient;
        this.vkSearchResultAnalyzer = vkSearchResultAnalyzer;
        this.searchExecutorService = createExecutorService(searchThreads);
        this.searchFromMusicRetries = searchFromMusicRetries;
    }

    @Override
    public List<VkSearchResponseDto> performSearch(SearchQueryDto searchQueryDto) {
        return search(searchQueryDto).stream()
                .filter(vkSearchResponseDto -> !isEmpty(vkSearchResponseDto.getList()))
                .collect(toList());
    }

    private List<VkSearchResponseDto> search(final SearchQueryDto searchQueryDto) {
        final CompletionService<List<VkSearchResponseDto>> searchCompletionService = submitSearch(searchQueryDto);
        return getSearchResult(searchCompletionService);
    }

    private CompletionService<List<VkSearchResponseDto>> submitSearch(SearchQueryDto searchQueryDto) {
        CompletionService<List<VkSearchResponseDto>> searchCompletionService = new ExecutorCompletionService<>(searchExecutorService);
        searchCompletionService.submit(() -> getSearchResultFromMusic(searchQueryDto));
        searchCompletionService.submit(() -> getSearchResultFromNewsFeed(searchQueryDto));
        return searchCompletionService;
    }

    private List<VkSearchResponseDto> getSearchResult(CompletionService<List<VkSearchResponseDto>> completionService) {
        try {
            final List<VkSearchResponseDto> searchResult = new ArrayList<>(2);

            consumeOne(completionService, searchResult);
            consumeOne(completionService, searchResult);

            return searchResult;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Can't perform search", e);
            return emptyList();
        }
    }

    private void consumeOne(CompletionService<List<VkSearchResponseDto>> completionService,
            List<VkSearchResponseDto> searchResult) throws InterruptedException, ExecutionException {
        ofNullable(completionService.take().get())
                .filter(this::hasResult)
                .ifPresent(searchResult::addAll);
    }

    private boolean hasResult(List<VkSearchResponseDto> searchResult) {
        return !isEmpty(searchResult) && searchResult.get(0) != null && !isEmpty(searchResult.get(0).getList());
    }

    private List<VkSearchResponseDto> getSearchResultFromNewsFeed(final SearchQueryDto searchQueryDto) {
        final Response response = vkClient.searchFromNewsFeed(searchQueryDto.getQuery());
        final VkSearchResponseDto vkSearchResponseDto = searchFromNewsFeedResponseToObject(response.body());
        if (searchQueryDto.isFilterByArtistTitle()) {
            vkSearchResultAnalyzer.filterByArtistTitle(searchQueryDto.getQuery(), vkSearchResponseDto);
        }
        vkSearchResponseDto.setFromNews(true);
        return singletonList(vkSearchResponseDto);
    }

    private List<VkSearchResponseDto> getSearchResultFromMusic(final SearchQueryDto searchQueryDto) {
        final List<VkSearchResponseDto> result = newArrayList();
        VkSearchResponseDto vkSearchResponseDto;
        String offset = "0";
        do {
            vkSearchResponseDto = getOptionalWithRetries(this::searchFromMusic, searchQueryDto.getQuery(), offset, searchFromMusicRetries, "vk_search_from_music")
                    .orElse(null);

            if (vkSearchResponseDto == null) {
                break;
            }

            offset = vkSearchResponseDto.getNextOffset();

            result.add(vkSearchResponseDto);
        } while (offset != null && vkSearchResponseDto.getHasMore());

        return result;
    }

    private Optional<VkSearchResponseDto> searchFromMusic(String searchQuery, String offset) {
        final Response response = vkClient.searchFromMusic(configuration.getVk().getUserId(), searchQuery, offset);
        return ofNullable(responseToObject(response.body(), VkSearchResponseDto.class));
    }
}
