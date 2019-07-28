package songbox.house.service;

import songbox.house.domain.dto.response.discogs.DiscogsReleaseDto;

import java.util.List;
import java.util.Optional;

public interface DiscogsWebsiteService {
    List<DiscogsReleaseDto> search(String query);

    Optional<DiscogsReleaseDto> getReleaseInfo(String discogsLink);
    List<String> searchArtworks(String searchQuery);
}
