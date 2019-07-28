package songbox.house.service.search;

import songbox.house.domain.dto.request.SearchRequestDto;
import songbox.house.domain.entity.Track;

import java.util.Optional;

public interface SearchDownloadServiceFacade {
    Optional<Track> searchAndDownload(final SearchRequestDto searchRequest);

    void searchAndDownloadAsync(final SearchRequestDto searchRequest);
}
