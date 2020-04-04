package songbox.house.service.search.youtube.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import songbox.house.client.YoutubeClient;
import songbox.house.domain.dto.request.SearchQueryDto;
import songbox.house.domain.dto.response.SearchResultDto;
import songbox.house.domain.dto.response.TrackMetadataDto;
import songbox.house.domain.dto.response.youtube.YoutubeSongDto;
import songbox.house.service.search.youtube.YoutubeSearchService;

import java.net.URI;
import java.util.List;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.net.URI.create;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getEncoder;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static songbox.house.util.Constants.EMPTY_URI;
import static songbox.house.util.Constants.PERFORMANCE_MARKER;
import static songbox.house.util.parser.YoutubeSearchParser.parseHtmlDocumentForSearch;

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
    public SearchResultDto search(SearchQueryDto query) {
        return new SearchResultDto(getTrackMetadataList(query));
    }

    @Override
    public SearchResultDto searchFast(SearchQueryDto searchQuery) {
        return new SearchResultDto(getTrackMetadataList(searchQuery));
    }

    private List<TrackMetadataDto> getTrackMetadataList(SearchQueryDto query) {
        if (enabled) {
            final long started = currentTimeMillis();
            try {
                Response response = client.search(query.getQuery());
                final List<TrackMetadataDto> result = parseHtmlDocumentForSearch(response.parse().toString())
                        .stream()
                        .map(this::toTrackMetadata)
                        .collect(toList());
                log.info(PERFORMANCE_MARKER, "Youtube search finished {}ms", currentTimeMillis() - started);
                return result;
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
        trackMetadataDto.setArtists(youtubeSongDto.getArtistsTitle().getArtists());
        trackMetadataDto.setTitle(youtubeSongDto.getArtistsTitle().getTitle());
        return trackMetadataDto;
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
