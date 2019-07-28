package songbox.house.client;

import org.jsoup.Connection.Response;

public interface DiscogsClient {
    Response getRelease(String releaseId);

    Response getReleaseByLink(String discogsLink);

    Response getLabelReleases(String labelId, Integer pageNumber, Integer perPage);

    Response getMarketplaceItem(String itemId);

    Response getUserCollectionItems(String userName, Integer pageNumber, Integer perPage);

    Response getArtistReleases(String artistId, Integer pageNumber, Integer perPage);

    Response searchArtwork(String searchQuery);

    Response search(String query);

    Response getUserWantListItems(String userName, int pageNumber, Integer releasesPageSize);

}
