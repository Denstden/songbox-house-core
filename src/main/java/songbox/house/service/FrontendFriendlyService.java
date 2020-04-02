package songbox.house.service;

import songbox.house.domain.dto.response.discogs.DiscogsReleaseDtoExt;
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
    List<DiscogsReleaseDtoExt> search(String query, boolean fetchResource);

    /**
     * Get detailed info
     *
     * @param discogsLink raw discogs link obtained from DiscogsReleaseDtoExt::discogsLink
     * @return detailed release
     */
    DiscogsReleaseDtoExt getDetailedInfo(String discogsLink, ProgressListener progressListener, boolean useFastSearch);

    DiscogsReleaseDtoExt saveToCollection(String discogsLink);

    List<DiscogsReleaseDtoExt> getSavedReleases();

    void deleteFromCollection(Long id);
}
