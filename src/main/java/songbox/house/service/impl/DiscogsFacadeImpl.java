package songbox.house.service.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.springframework.stereotype.Service;
import songbox.house.client.DiscogsClient;
import songbox.house.domain.dto.response.discogs.DiscogsLabelReleasesDto;
import songbox.house.domain.dto.response.discogs.DiscogsMarketPlaceListingResponseDto;
import songbox.house.domain.dto.response.discogs.DiscogsReleaseDto;
import songbox.house.domain.dto.response.discogs.DiscogsReleaseResponseDto;
import songbox.house.domain.dto.response.discogs.DiscogsReleasesPageableDto;
import songbox.house.domain.dto.response.discogs.DiscogsUserWantListDto;
import songbox.house.exception.DiscogsException;
import songbox.house.exception.InvalidDiscogsLinkException;
import songbox.house.service.DiscogsFacade;

import java.util.Optional;

import static java.text.MessageFormat.format;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static songbox.house.util.JsonUtils.fromString;
import static songbox.house.util.parser.DiscogsLinkParser.parseAsArtist;
import static songbox.house.util.parser.DiscogsLinkParser.parseAsLabel;
import static songbox.house.util.parser.DiscogsLinkParser.parseAsMarketplaceItem;
import static songbox.house.util.parser.DiscogsLinkParser.parseAsRelease;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Slf4j
public class DiscogsFacadeImpl implements DiscogsFacade {
    private static final Integer RELEASES_PAGE_SIZE = 100;

    DiscogsClient discogsClient;

    public Long extractArtistId(String artistLink) {
        if (artistLink.contains(ARTIST_DETERMINER)) {
            return parseAsArtist(artistLink);
        }
        throw new InvalidDiscogsLinkException(format("Discogs link should contain {0} substring.", ARTIST_DETERMINER));
    }

    public DiscogsReleasesPageableDto getArtistReleases(final String artistId, final int pageNumber) {
        final Connection.Response userCollectionItems = discogsClient.getArtistReleases(artistId, pageNumber, RELEASES_PAGE_SIZE);
        return fromString(userCollectionItems.body(), DiscogsReleasesPageableDto.class);
    }

    public DiscogsReleasesPageableDto getUserCollectionReleases(final String userName, final int pageNumber) {
        final Connection.Response userCollectionItems = discogsClient.getUserCollectionItems(userName, pageNumber, RELEASES_PAGE_SIZE);
        return fromString(userCollectionItems.body(), DiscogsReleasesPageableDto.class);
    }

    public DiscogsUserWantListDto getUserWantListReleaseIds(final String userName, final int pageNumber) {
        final Connection.Response userCollectionItems = discogsClient.getUserWantListItems(userName, pageNumber, RELEASES_PAGE_SIZE);
        return fromString(userCollectionItems.body(), DiscogsUserWantListDto.class);
    }

    public String extractLabelId(final String labelLink) {
        if (labelLink.contains(LABEL_DETERMINER)) {
            return parseAsLabel(labelLink).toString();
        }
        throw new InvalidDiscogsLinkException(format("Discogs link should contain {0} substring.", LABEL_DETERMINER));
    }

    public DiscogsLabelReleasesDto getLabelReleases(final String labelId,
            final Integer pageNumber) {
        final Connection.Response response = discogsClient.getLabelReleases(labelId, pageNumber, RELEASES_PAGE_SIZE);
        return fromString(response.body(), DiscogsLabelReleasesDto.class);
    }

    public String getReleaseId(final String link) {
        log.debug("Processing discogs link {}", link);

        final Long releaseId;

        if (link.contains(MARKETPLACE_DETERMINER)) {
            releaseId = getReleaseIdFromMarketplaceLink(link);
        } else if (link.contains(DATABASE_DETERMINER)) {
            releaseId = parseAsRelease(link);
            log.debug("Parsed release id {}", releaseId);
        } else {
            throw new InvalidDiscogsLinkException(format("Discogs link should contain one of ({0},{1}) substrings.", MARKETPLACE_DETERMINER, DATABASE_DETERMINER));
        }

        return releaseId.toString();
    }

    public Long getReleaseIdFromMarketplaceLink(final String link) {
        final String itemId = parseAsMarketplaceItem(link).toString();
        log.debug("Parsed item id {}", itemId);

        final Connection.Response response = discogsClient.getMarketplaceItem(itemId);
        final DiscogsMarketPlaceListingResponseDto responseDto = fromString(response.body(), DiscogsMarketPlaceListingResponseDto.class);

        return ofNullable(responseDto)
                .map(DiscogsMarketPlaceListingResponseDto::getRelease)
                .map(DiscogsReleaseDto::getId)
                .orElseThrow(() -> new DiscogsException(format("Can't parse DiscogsMarketPlaceListingResponseDto from {0}", response.body())));
    }

    public Optional<DiscogsReleaseResponseDto> getReleaseDto(final String releaseId) {
        try {
            final Connection.Response response = discogsClient.getRelease(releaseId);
            return ofNullable(fromString(response.body(), DiscogsReleaseResponseDto.class));
        } catch (final Exception e) {
            log.error("Error getting release by id {}", releaseId);
            return empty();
        }
    }

}
