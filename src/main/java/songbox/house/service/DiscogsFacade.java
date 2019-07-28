package songbox.house.service;

import songbox.house.domain.dto.response.discogs.*;

import java.util.Optional;


public interface DiscogsFacade {
    String DEFAULT_ARTISTS_DELIMITER = " & ";
    String MARKETPLACE_DETERMINER = "sell";
    String DATABASE_DETERMINER = "release";
    String LABEL_DETERMINER = "label";
    String ARTIST_DETERMINER = "artist";

    Long extractArtistId(String artistLink);

    DiscogsReleasesPageableDto getArtistReleases(final String artistId, final int pageNumber);

    DiscogsReleasesPageableDto getUserCollectionReleases(final String userName, final int pageNumber);

    DiscogsUserWantListDto getUserWantListReleaseIds(final String userName, final int pageNumber);

    String extractLabelId(final String labelLink);

    DiscogsLabelReleasesDto getLabelReleases(final String labelId, final Integer pageNumber);

    String getReleaseId(final String link);

    Long getReleaseIdFromMarketplaceLink(final String link);

    Optional<DiscogsReleaseResponseDto> getReleaseDto(final String releaseId);
}
