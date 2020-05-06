package songbox.house.service.search.youtube.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import songbox.house.client.YoutubeClient;
import songbox.house.domain.dto.request.SearchQueryDto;
import songbox.house.domain.dto.response.SearchResultDto;
import songbox.house.domain.dto.response.TrackMetadataDto;
import songbox.house.domain.dto.response.youtube.YoutubeSongDto;
import songbox.house.service.search.youtube.YoutubeSearchService;
import songbox.house.util.ArtistsTitle;
import songbox.house.util.Measurable;
import songbox.house.util.compare.TrackMetadataComparator;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.net.URI.create;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getEncoder;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static songbox.house.util.BitRateCalculator.calculateBitRate;
import static songbox.house.util.Constants.EMPTY_URI;
import static songbox.house.util.parser.YoutubeSearchParser.parseHtmlDocumentForSearch;
import static songbox.house.util.parser.YoutubeSearchParser.parseSizeMb;

@Service
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class YoutubeSearchServiceImpl implements YoutubeSearchService {

    private static final Short YOUTUBE_BITRATE = 128;

    YoutubeClient client;
    Boolean enabled;

    public YoutubeSearchServiceImpl(YoutubeClient client,
            @Value("${songbox.house.youtube.search.enabled:false}") Boolean enabled) {
        this.client = client;
        this.enabled = enabled;
    }

    @Override
    @Measurable
    public SearchResultDto search(SearchQueryDto query) {
        return new SearchResultDto(getTrackMetadataList(query));
    }

    @Override
    @Measurable
    public SearchResultDto searchFast(SearchQueryDto searchQuery) {
        return new SearchResultDto(getTrackMetadataList(searchQuery));
    }

    @Override
    public Optional<TrackMetadataDto> searchForPreview(SearchQueryDto query) {
        return search(query).getSongs().stream().findFirst();
    }

    private List<TrackMetadataDto> getTrackMetadataList(SearchQueryDto query) {
        if (enabled && query.isLowQuality()) {
            try {
                Response response = client.search(query.getQuery());
                return parseHtmlDocumentForSearch(response.parse().toString())
                        .parallelStream()
                        .map(this::toTrackMetadata)
                        .sorted(new TrackMetadataComparator(ArtistsTitle.parse(query.getQuery()), 70))
                        .collect(toList());
            } catch (Exception e) {
                log.error("Can't execute youtube search", e);
            }
        }
        return emptyList();
    }

    private TrackMetadataDto toTrackMetadata(YoutubeSongDto youtubeSongDto) {
        TrackMetadataDto trackMetadataDto = new TrackMetadataDto();
        trackMetadataDto.setResource("Youtube");
        trackMetadataDto.setUri(getUri(youtubeSongDto.getVideoId()).toASCIIString());
        trackMetadataDto.setThumbnail(youtubeSongDto.getThumbnail());
        trackMetadataDto.setBitRate(YOUTUBE_BITRATE);
        trackMetadataDto.setDurationSec(youtubeSongDto.getDuration());
        trackMetadataDto.setArtistsTitle(youtubeSongDto.getArtistsTitle());
        getSizeMb(youtubeSongDto).ifPresent(sizeMb -> {
            long sizeBytes = Double.valueOf(sizeMb * 1024 * 1024).longValue();
            trackMetadataDto.setBitRate(calculateBitRate(youtubeSongDto.getDuration(), sizeBytes));
            trackMetadataDto.setSizeMb(sizeMb);
        });
        return trackMetadataDto;
    }

    private Optional<Double> getSizeMb(YoutubeSongDto songDto) {
        final String videoId = songDto.getVideoId();
        if (StringUtils.isNotBlank(videoId)) {
            try {
                String html = client.getTrackMetadata(videoId).parse().toString();
                return parseSizeMb(html);
            } catch (Exception e) {
                log.info("Can't get track metadata for video {}", videoId, e);
            }
        }
        return Optional.empty();
    }

    private URI getUri(String videoId) {
        if (videoId.isEmpty()) {
            return create("");
        }
        try {
            String base64 = new String(getEncoder().encode(videoId.getBytes()), UTF_8);
            String uriString = format("%s:%s", resourceName(), base64);
            return create(uriString);
        } catch (Exception e) {
            log.error("", e);
        }
        return EMPTY_URI;
    }
}
