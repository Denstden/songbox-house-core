package songbox.house.service.search.vk.impl;

import com.google.api.client.util.Lists;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import songbox.house.domain.dto.request.vk.VkSearchAudioRequestDto;
import songbox.house.domain.dto.response.TrackMetadataDto;
import songbox.house.domain.dto.response.vk.VkSearchResponseDto;
import songbox.house.domain.entity.SearchHistory;
import songbox.house.domain.entity.VkAudio;
import songbox.house.service.BitRateAndSizeService;
import songbox.house.service.DiscogsWebsiteService;
import songbox.house.service.KafkaService;
import songbox.house.service.search.SearchHistoryService;
import songbox.house.service.search.SearchQueryDto;
import songbox.house.service.search.vk.VkAudioLoader;
import songbox.house.service.search.vk.VkSearchPerformer;
import songbox.house.service.search.vk.VkSearchResultAnalyzer;
import songbox.house.service.search.vk.VkSearchResultProcessor;
import songbox.house.service.search.vk.VkSearchService;
import songbox.house.util.compare.BitRateDurationComparator;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.net.URI.create;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getEncoder;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.util.CollectionUtils.isEmpty;
import static songbox.house.util.Constants.EMPTY_URI;
import static songbox.house.util.Constants.PERFORMANCE_MARKER;

@Service
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class VKSearchServiceImpl implements VkSearchService {
    private static String AUTHOR_TITLE_DELIMITER = " - ";

    VkSearchPerformer vkSearchPerformer;
    VkSearchResultProcessor vkSearchResultProcessor;
    VkSearchResultAnalyzer vkSearchResultAnalyzer;
    VkAudioLoader vkAudioLoader;
    BitRateAndSizeService bitRateAndSizeService;
    DiscogsWebsiteService discogsWebsiteService;

    SearchHistoryService searchHistoryService;
    KafkaService kafkaService;

    BitRateDurationComparator bitRateDurationComparator = new BitRateDurationComparator();
    Boolean loadAllEnabled;
    Boolean calculateBitRatesForSearch;

    public VKSearchServiceImpl(VkSearchPerformer vkSearchPerformer, VkSearchResultProcessor vkSearchResultProcessor,
            VkSearchResultAnalyzer vkSearchResultAnalyzer, VkAudioLoader vkAudioLoader,
            DiscogsWebsiteService discogsWebsiteService,
            SearchHistoryService searchHistoryService, KafkaService kafkaService,
            BitRateAndSizeService bitRateAndSizeService,
            @Value("${songbox.house.vk.download.load_all.enabled}") Boolean loadAllEnabled,
            @Value("${songbox.house.vk.search.calc_bitrate}") Boolean calculateBitRatesForSearch) {
        this.vkSearchPerformer = vkSearchPerformer;
        this.vkSearchResultProcessor = vkSearchResultProcessor;
        this.vkSearchResultAnalyzer = vkSearchResultAnalyzer;
        this.vkAudioLoader = vkAudioLoader;
        this.discogsWebsiteService = discogsWebsiteService;
        this.searchHistoryService = searchHistoryService;
        this.kafkaService = kafkaService;
        this.bitRateAndSizeService = bitRateAndSizeService;
        this.loadAllEnabled = loadAllEnabled;
        this.calculateBitRatesForSearch = calculateBitRatesForSearch;
    }

    @Override
    public List<TrackMetadataDto> search(SearchQueryDto query) {
        final long searchStarted = currentTimeMillis();
        final List<VkSearchResponseDto> searchResults = vkSearchPerformer.performSearch(query);
        if (searchResults.isEmpty()) {
            kafkaService.sendToFailedQueries(query.getQuery());
            return emptyList();
        } else {
            return processSearchResult(searchResults, query, searchStarted);
        }
    }

    private List<TrackMetadataDto> processSearchResult(List<VkSearchResponseDto> searchResults, SearchQueryDto query,
            long searchStarted) {
        final VkSearchAudioRequestDto vkSearchAudioRequestDto = vkSearchResultProcessor.processSearchResults(searchResults);

        final List<VkAudio> audios = vkAudioLoader.load(vkSearchAudioRequestDto, false);
        //TODO do we need more than 1 artwork?
        if (isEmpty(audios)) {
            return newArrayList();
        } else {
            final List<String> artworks = isEmpty(audios) || !query.isFetchArtwork() ?
                    Lists.newArrayList() : discogsWebsiteService.searchArtworks(query.getQuery());


            List<VkAudio> filtered = vkSearchResultAnalyzer.filterIncorrectResults(audios, query.getQuery());
            if (calculateBitRatesForSearch) {
                filtered = bitRateAndSizeService.calculateBitRatesAndSize(audios);
            }

            log.info(PERFORMANCE_MARKER, "Vk Search+Loading {}ms", currentTimeMillis() - searchStarted);
            return filtered
                    .stream()
                    .map(e -> {
                        String thumbnail = artworks.isEmpty() ? "" : artworks.get(0);
                        return new TrackMetadataDto(
                                e.getArtist().trim(), e.getTitle().trim(),
                                e.getDuration(),
                                e.getBitRate(),
                                thumbnail,
                                getUri(e), resourceName());
                    })
                    .collect(toList());
        }
    }

    @Override
    @SneakyThrows
    public Optional<VkAudio> searchForDownloading(final String authors, final String title,
            final SearchHistory searchHistory,
            final boolean only320) {
        final String searchQuery = authors + AUTHOR_TITLE_DELIMITER + title;
        final List<VkSearchResponseDto> searchResults = vkSearchPerformer.performSearch(new SearchQueryDto(searchQuery));

        final VkSearchAudioRequestDto vkSearchAudioRequestDto = vkSearchResultProcessor.processSearchResults(searchResults);

        final List<VkAudio> loaded = vkAudioLoader.load(vkSearchAudioRequestDto, loadAllEnabled);

        if (loaded.isEmpty()) {
            searchHistoryService.saveFail(searchHistory);
            log.debug("Empty search result by query {}", searchQuery);
            return empty();
        } else {
            final List<VkAudio> filtered = vkSearchResultAnalyzer.filterIncorrectResults(loaded, authors, title, only320);
            final List<VkAudio> withBitRates = bitRateAndSizeService.calculateBitRatesAndSize(filtered);
            sortByBitRateAndDuration(withBitRates);
            if (withBitRates.isEmpty()) {
                kafkaService.sendToFailedQueries(searchQuery);
                return empty();
            }

            log.debug("Success search by query \"{}\"", searchQuery);
            VkAudio vkAudio = withBitRates.get(0);
            searchHistoryService.saveSuccess(searchHistory, getUri(vkAudio).toString());

            addArtwork(searchQuery, vkAudio);

            return of(vkAudio);
        }
    }

    private void addArtwork(String searchQuery, VkAudio vkAudio) {
        try {
            List<String> artworks = discogsWebsiteService.searchArtworks(searchQuery);
            if (!isEmpty(artworks)) {
                vkAudio.setArtworkSrc(artworks.get(0));
            }
        } catch (Exception e) {
            log.warn("Exception during searching artwork", e);
        }
    }

    private void sortByBitRateAndDuration(final List<VkAudio> list) {
        list.sort(bitRateDurationComparator.reversed());
    }

    private URI getUri(VkAudio vkAudio) {
        try {
            String base64 = new String(getEncoder().encode(vkAudio.getUrl().getBytes()), UTF_8);
            return create(format("%s:%s", resourceName(), base64));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return EMPTY_URI;
    }
}
