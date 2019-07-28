package songbox.house.service;

import songbox.house.domain.dto.response.DiscogsTrackListResponseDto;
import songbox.house.domain.dto.response.SearchAndDownloadResponseDto;

import java.util.List;

public interface DiscogsService {
    SearchAndDownloadResponseDto searchAndDownload(String link, Long collectionId);

    void searchAndDownloadAsync(String link, Long collectionId);

    void searchAndDownloadLinksAsync(String links, Long collectionId, String separator);

    List<DiscogsTrackListResponseDto> getTrackList(String link);

    void searchAndDownloadLabelTracks(String labelLink, Long collectionId, boolean only320);

    void searchAndDownloadUserDiscogsCollection(String userName, String collectionName);

    void searchAndDownloadArtistReleases(String link, Long collectionId, boolean only320);

    void searchAndDownloadUserWantList(String userName, String collectionName);
}
