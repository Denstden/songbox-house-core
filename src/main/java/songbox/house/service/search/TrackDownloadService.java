package songbox.house.service.search;

import songbox.house.domain.dto.request.SaveSongsDto;
import songbox.house.domain.dto.request.SearchRequestDto;
import songbox.house.domain.dto.response.SongDto;
import songbox.house.domain.dto.response.TrackDto;

import java.util.Optional;

public interface TrackDownloadService {
    Optional<TrackDto> searchAndDownload(final SearchRequestDto searchRequest);

    void searchAndDownloadAsync(final SearchRequestDto searchRequest);

    Iterable<TrackDto> download(SaveSongsDto saveSongsDto);

    TrackDto download(SongDto songDto, Long collectionId);
}
