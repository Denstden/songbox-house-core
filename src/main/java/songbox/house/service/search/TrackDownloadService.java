package songbox.house.service.search;

import songbox.house.domain.dto.request.SaveSongsDto;
import songbox.house.domain.dto.request.SearchRequestDto;
import songbox.house.domain.dto.response.SongDto;
import songbox.house.domain.dto.response.TrackDto;
import songbox.house.domain.dto.response.TrackMetadataDto;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public interface TrackDownloadService {
    Optional<TrackDto> searchAndDownload(final SearchRequestDto searchRequest);

    Optional<TrackDto> download(TrackMetadataDto trackMetadataDto, Long collectionId, Long ownerId,
            Set<String> genres);

    default CompletableFuture<Optional<TrackDto>> downloadAsync(TrackMetadataDto trackMetadataDto,
            Long collectionId, Long ownerId, Set<String> genres) {

        return supplyAsync(() -> download(trackMetadataDto, collectionId, ownerId, genres));
    }

    void searchAndDownloadAsync(final SearchRequestDto searchRequest);

    Iterable<TrackDto> download(SaveSongsDto saveSongsDto);

    TrackDto download(SongDto songDto, Long collectionId);
}
