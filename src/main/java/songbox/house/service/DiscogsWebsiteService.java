package songbox.house.service;

import songbox.house.domain.dto.response.discogs.DiscogsReleaseDtoExt;

import java.util.List;
import java.util.Optional;

public interface DiscogsWebsiteService {
    List<DiscogsReleaseDtoExt> search(String query);

    Optional<DiscogsReleaseDtoExt> getReleaseInfo(String discogsLink);
    List<String> searchArtworks(String searchQuery);
}
