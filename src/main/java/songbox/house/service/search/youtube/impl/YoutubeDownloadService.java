package songbox.house.service.search.youtube.impl;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import songbox.house.client.YoutubeClient;
import songbox.house.domain.dto.request.SearchQueryDto;
import songbox.house.domain.dto.response.SearchResultDto;
import songbox.house.domain.dto.response.TrackDto;
import songbox.house.domain.dto.response.TrackMetadataDto;
import songbox.house.service.download.DownloadService;
import songbox.house.service.search.youtube.YoutubeSearchService;
import songbox.house.util.ArtistsTitle;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.slf4j.LoggerFactory.getLogger;
import static songbox.house.util.DownloadUtil.downloadBytes;
import static songbox.house.util.parser.YoutubeSearchParser.parseMp3Url;

@Service
public class YoutubeDownloadService implements DownloadService {

    private static final Logger LOGGER = getLogger(YoutubeDownloadService.class);

    private static final String FILE_NAME_REGEX = "[!\"#$%&'()*+,\\-/:;<=>?@\\[\\]^_`{|}~]";

    private final YoutubeClient youtubeClient;
    private final YoutubeSearchService youtubeSearchService;
    private final Integer youtubeDownloadPriority;

    public YoutubeDownloadService(YoutubeClient youtubeClient,
            YoutubeSearchService youtubeSearchService,
            @Value("${songbox.house.youtube.download.priority:50}") Integer youtubeDownloadPriority) {
        this.youtubeClient = youtubeClient;
        this.youtubeSearchService = youtubeSearchService;
        this.youtubeDownloadPriority = youtubeDownloadPriority;
    }

    @Override
    public Optional<TrackDto> download(SearchQueryDto searchQuery) {
        return download(searchQuery, null);
    }

    @Override
    public Optional<TrackDto> download(SearchQueryDto searchQuery, String artworkUrl) {
        return ofNullable(youtubeSearchService.search(searchQuery))
                .map(SearchResultDto::getSongs)
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(list.size() - 1))
                .map(trackMetadataDto -> {
                    trackMetadataDto.setThumbnail(artworkUrl);
                    return trackMetadataDto;
                })
                .flatMap(this::download);
    }

    @Override
    public Optional<TrackDto> download(TrackMetadataDto trackMetadataDto) {
        try {
            String url = new String(Base64.getDecoder().decode(trackMetadataDto.getUri().split(":")[1]), UTF_8);
            return download(url, trackMetadataDto);
        } catch (IOException e) {
            LOGGER.error("Can't download from youtube {}", trackMetadataDto, e);
            return empty();
        }
    }

    @Override
    public int getDownloadPriority() {
        return youtubeDownloadPriority;
    }

    @Override
    public boolean canDownload(String serviceName) {
        return "Youtube".equals(serviceName);
    }

    // format /watch?v=QsE0STLkskk
    private Optional<TrackDto> download(String videoId, TrackMetadataDto trackMetadataDto) throws IOException {
        String html = youtubeClient.getTrackMetadata(videoId).parse().toString();
        return parseMp3Url(html).flatMap(url -> toTrackDto(url, trackMetadataDto));
    }

    private Optional<TrackDto> toTrackDto(String url, TrackMetadataDto trackMetadataDto) {
        return downloadBytes(url).map(content -> {
            TrackDto trackDto = new TrackDto();
            trackDto.setArtists(trackMetadataDto.getArtistsTitle().getArtists());
            trackDto.setTitle(trackMetadataDto.getArtistsTitle().getTitle());
            trackDto.setContent(content);
            trackDto.setDurationSec(trackMetadataDto.getDurationSec());
            trackDto.setExtension("mp3");
            trackDto.setFileName(getFilename(trackMetadataDto));
            long sizeBytes = (long) content.length;
            trackDto.setSizeBytes(sizeBytes);
            trackDto.setBitRate(trackMetadataDto.getBitRate());
            trackDto.setArtworkUrl(trackMetadataDto.getThumbnail());
            return trackDto;
        });
    }

    //TODO move to TrackMetadataDto
    private String getFilename(TrackMetadataDto trackMetadataDto) {
        StringBuilder sb = new StringBuilder();

        ArtistsTitle artistsTitle = trackMetadataDto.getArtistsTitle();
        String formattedArtist = artistsTitle.getArtists().trim().replaceAll(FILE_NAME_REGEX, "");
        String formattedTitle = artistsTitle.getTitle().trim().replaceAll(FILE_NAME_REGEX, "");

        sb.append(substring(formattedArtist, 0, 40));
        sb.append(" - ");
        sb.append(substring(formattedTitle, 0, 50));

        sb.append(".mp3");

        return sb.toString();
    }
}
