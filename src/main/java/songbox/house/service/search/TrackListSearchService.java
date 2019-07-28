package songbox.house.service.search;

import songbox.house.domain.dto.TrackListSearchRequestDto;
import songbox.house.domain.dto.response.SearchAndDownloadResponseDto;

public interface TrackListSearchService {

    SearchAndDownloadResponseDto searchAndDownloadTrackList(final TrackListSearchRequestDto requestDto);

    void searchAndDownloadTrackListAsync(final TrackListSearchRequestDto requestDto);
}
