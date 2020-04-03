package songbox.house.service.search.youtube.impl;

import org.springframework.stereotype.Service;
import songbox.house.domain.dto.request.SearchQueryDto;
import songbox.house.domain.dto.response.TrackDto;
import songbox.house.domain.dto.response.TrackMetadataDto;
import songbox.house.service.download.DownloadService;

import java.util.Optional;

@Service
public class YoutubeDownloadService implements DownloadService {
    @Override
    public Optional<TrackDto> download(SearchQueryDto searchQuery) {
        return download(searchQuery, null);
    }

    @Override
    public Optional<TrackDto> download(SearchQueryDto searchQuery, String artworkUrl) {
        return Optional.empty();
    }

    @Override
    public Optional<TrackDto> download(TrackMetadataDto trackMetadataDto) {
        return Optional.empty();
    }

    @Override
    public boolean isDownloadEnabled() {
        return false;
    }
}
