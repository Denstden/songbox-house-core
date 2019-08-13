package songbox.house.service;

import songbox.house.domain.dto.response.discogs.DiscogsReleaseDto;
import songbox.house.domain.entity.MusicCollection;
import songbox.house.util.ProgressListener;

import java.util.List;

public interface FrontendFriendlyService {
    /**
     * Search by query on Discogs
     *
     * @param query         Search Query
     * @param fetchResource if true response will contains links to audio resource, but it may be longer
     * @return search result releases
     */
    List<DiscogsReleaseDto> search(String query, boolean fetchResource);

    /**
     * Get detailed info
     *
     * @param discogsLink raw discogs link obtained from DiscogsReleaseDto::discogsLink
     * @return detailed release
     */
    DiscogsReleaseDto getDetailedInfo(String discogsLink, ProgressListener progressListener);

    DiscogsReleaseDto saveToCollection(String discogsLink);

    List<DiscogsReleaseDto> getSavedReleases();

    void deleteFromCollection(Long id);
}
