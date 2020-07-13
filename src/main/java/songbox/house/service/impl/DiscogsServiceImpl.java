package songbox.house.service.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection.Response;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import songbox.house.client.DiscogsClient;
import songbox.house.domain.dto.response.DiscogsTrackListResponseDto;
import songbox.house.domain.dto.response.SearchAndDownloadResponseDto;
import songbox.house.domain.dto.response.discogs.DiscogsLabelReleasesDto;
import songbox.house.domain.dto.response.discogs.DiscogsMarketPlaceListingResponseDto;
import songbox.house.domain.dto.response.discogs.DiscogsReleaseDto;
import songbox.house.domain.dto.response.discogs.DiscogsReleaseDtoExt;
import songbox.house.domain.dto.response.discogs.DiscogsReleaseResponseDto;
import songbox.house.domain.dto.response.discogs.DiscogsReleasesPageableDto;
import songbox.house.domain.dto.response.discogs.DiscogsTrackDto;
import songbox.house.domain.dto.response.discogs.DiscogsUserWantListDto;
import songbox.house.domain.dto.response.discogs.DiscogsUserWantListItemDto;
import songbox.house.domain.dto.response.discogs.ReleasePageable;
import songbox.house.domain.entity.MusicCollection;
import songbox.house.exception.DiscogsException;
import songbox.house.exception.InvalidDiscogsLinkException;
import songbox.house.service.DiscogsService;
import songbox.house.service.MusicCollectionService;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;
import static songbox.house.util.DiscogsArtistsUtil.concatArtists;
import static songbox.house.util.JsonUtils.fromString;
import static songbox.house.util.parser.DiscogsLinkParser.parseAsArtist;
import static songbox.house.util.parser.DiscogsLinkParser.parseAsLabel;
import static songbox.house.util.parser.DiscogsLinkParser.parseAsMarketplaceItem;
import static songbox.house.util.parser.DiscogsLinkParser.parseAsRelease;

@Service
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class DiscogsServiceImpl implements DiscogsService {
    private static Integer RELEASES_PAGE_SIZE = 100;

    public static final String MARKETPLACE_DETERMINER = "sell";
    public static final String DATABASE_DETERMINER = "release";
    public static final String LABEL_DETERMINER = "label";
    public static final String ARTIST_DETERMINER = "artist";

    DiscogsClient discogsClient;
    MusicCollectionService collectionService;
    DiscogsReleaseProcessingService releaseProcessingService;

    @Override
    public SearchAndDownloadResponseDto searchAndDownload(String link, Long collectionId) {
        String releaseId = getReleaseId(link);

        return getReleaseDto(releaseId)
                .map(rel -> releaseProcessingService.processDiscogsRelease(rel, collectionId, false))
                .orElseThrow(() -> new DiscogsException("Error getting release by link " + link));
    }

    @Override
    @Async
    public void searchAndDownloadAsync(String link, Long collectionId) {
        String releaseId = getReleaseId(link);

        processDiscogsReleaseId(releaseId, collectionId, false);
    }

    @Override
    @Async
    public void searchAndDownloadLinksAsync(String links, Long collectionId, String separator) {
        List<String> releaseIds = getReleaseIds(links, separator);

        releaseIds.forEach(releaseId -> processDiscogsReleaseId(releaseId, collectionId, false));
    }

    @Override
    public List<DiscogsTrackListResponseDto> getTrackList(String link) {
        String releaseId = getReleaseId(link);

        return getReleaseDto(releaseId)
                .map(release -> toTrackListDto(release.getTracklist(), concatArtists(release.getArtists())))
                .orElseThrow(() -> new DiscogsException("Error getting track list by link " + link));
    }

    @Override
    @Async
    @SneakyThrows
    public void searchAndDownloadLabelTracks(String labelLink, Long collectionId, boolean only320) {
        String labelId = extractLabelId(labelLink);

        processDiscogsPageableRequest(collectionId, labelId, this::getLabelReleases, false);

        log.info("Processing label releases finished.");
    }

    @Override
    @Async
    public void searchAndDownloadUserDiscogsCollection(String userName, String collectionName) {
        MusicCollection collection = collectionService.getOrCreate(collectionName);

        processDiscogsPageableRequest(collection.getCollectionId(), userName, this::getUserCollectionReleases, false);

        log.info("Processing user collection finished.");
    }

    @Override
    @Async
    public void searchAndDownloadArtistReleases(String link, Long collectionId, boolean only320) {
        String userId = extractArtistId(link).toString();

        processDiscogsPageableRequest(collectionId, userId, this::getArtistReleases, only320);

        log.info("Processing artist releases finished.");
    }

    @Override
    @Async
    public void searchAndDownloadUserWantList(String userName, String collectionName) {
        MusicCollection collection = collectionService.getOrCreate(collectionName);

        int pageNumber = 1;
        int countPages;
        int i = 1;

        do {
            DiscogsUserWantListDto userWantListReleaseIds = getUserWantListReleaseIds(userName, pageNumber);
            countPages = userWantListReleaseIds.getPagination().getPages();

            userWantListReleaseIds.getWants().stream()
                    .map(DiscogsUserWantListItemDto::getId)
                    .forEach(id -> processDiscogsReleaseId(id.toString(), collection.getCollectionId(), false));
            log.info("Processed {} page of {}", pageNumber, countPages);

            pageNumber = countPages - i + 1;
        } while (i++ != countPages);

        log.info("Processing user want list finished.");
    }

    private <T extends ReleasePageable> void processDiscogsPageableRequest(long collectionId, String entityId,
            BiFunction<String, Integer, T> getDtoFunction,
            boolean only320) {

        int pageNumber = 1;
        int countPages;

        do {
            T releasesPageable = getDtoFunction.apply(entityId, pageNumber);
            countPages = releasesPageable.getPagination().getPages();

            processReleases(releasesPageable.getReleases(), collectionId, only320);
            log.info("Processed {} page of {}", pageNumber, countPages);

            pageNumber++;
        } while (pageNumber < countPages);
    }

    private void processReleases(List<DiscogsReleaseDto> releases, Long collectionId, boolean only320) {
        for (DiscogsReleaseDto releaseDto : releases) {
            getReleaseDto(releaseDto.getId().toString())
                    .ifPresent(release -> releaseProcessingService.processDiscogsRelease(release, collectionId, only320));
        }
    }

    private void processDiscogsReleaseId(String releaseId, Long collectionId, boolean only320) {
        getReleaseDto(releaseId)
                .ifPresent(rel -> releaseProcessingService.processDiscogsRelease(rel, collectionId, only320));
    }

    private List<String> getReleaseIds(String links, String separator) {
        String[] splitted = links.split(separator);

        return stream(splitted)
                .map(this::getReleaseId)
                .collect(toList());
    }

    private Optional<DiscogsReleaseResponseDto> getReleaseDto(String releaseId) {
        try {
            Response response = discogsClient.getRelease(releaseId);
            return ofNullable(fromString(response.body(), DiscogsReleaseResponseDto.class));
        } catch (Exception e) {
            log.error("Error getting release by id {}", releaseId);
            return empty();
        }
    }

    private Long extractArtistId(String artistLink) {
        if (artistLink.contains(ARTIST_DETERMINER)) {
            return parseAsArtist(artistLink);
        }
        throw new InvalidDiscogsLinkException(format("Discogs link should contain {0} substring.", ARTIST_DETERMINER));
    }

    private DiscogsReleasesPageableDto getArtistReleases(String artistId, int pageNumber) {
        Response userCollectionItems = discogsClient.getArtistReleases(artistId, pageNumber, RELEASES_PAGE_SIZE);
        return fromString(userCollectionItems.body(), DiscogsReleasesPageableDto.class);
    }

    private DiscogsReleasesPageableDto getUserCollectionReleases(String userName, int pageNumber) {
        Response userCollectionItems = discogsClient.getUserCollectionItems(userName, pageNumber, RELEASES_PAGE_SIZE);
        return fromString(userCollectionItems.body(), DiscogsReleasesPageableDto.class);
    }

    private DiscogsUserWantListDto getUserWantListReleaseIds(String userName, int pageNumber) {
        Response userCollectionItems = discogsClient.getUserWantListItems(userName, pageNumber, RELEASES_PAGE_SIZE);
        return fromString(userCollectionItems.body(), DiscogsUserWantListDto.class);
    }

    private String extractLabelId(String labelLink) {
        if (labelLink.contains(LABEL_DETERMINER)) {
            return parseAsLabel(labelLink).toString();
        }
        throw new InvalidDiscogsLinkException(format("Discogs link should contain {0} substring.", LABEL_DETERMINER));
    }

    private DiscogsLabelReleasesDto getLabelReleases(String labelId,
            Integer pageNumber) {
        Response response = discogsClient.getLabelReleases(labelId, pageNumber, RELEASES_PAGE_SIZE);
        return fromString(response.body(), DiscogsLabelReleasesDto.class);
    }

    private String getReleaseId(String link) {
        log.debug("Processing discogs link {}", link);

        Long releaseId;

        if (link.contains(MARKETPLACE_DETERMINER)) {
            releaseId = getReleaseIdFromMarketplaceLink(link);
        } else if (link.contains(DATABASE_DETERMINER)) {
            releaseId = parseAsRelease(link);
            log.debug("Parsed release id {}", releaseId);
        } else {
            throw new InvalidDiscogsLinkException(MessageFormat.format("Discogs link should contain one of " +
                    "({0},{1}) substrings.", MARKETPLACE_DETERMINER, DATABASE_DETERMINER));
        }

        return releaseId.toString();
    }

    private Long getReleaseIdFromMarketplaceLink(String link) {
        String itemId = parseAsMarketplaceItem(link).toString();
        log.debug("Parsed item id {}", itemId);

        Response response = discogsClient.getMarketplaceItem(itemId);
        DiscogsMarketPlaceListingResponseDto responseDto = fromString(response.body(), DiscogsMarketPlaceListingResponseDto.class);

        return ofNullable(responseDto)
                .map(DiscogsMarketPlaceListingResponseDto::getRelease)
                .map(DiscogsReleaseDtoExt::getId)
                .orElseThrow(() -> new DiscogsException(MessageFormat.format("Can't parse " +
                        "DiscogsMarketPlaceListingResponseDto from {0}", response.body())));
    }

    private List<DiscogsTrackListResponseDto> toTrackListDto(List<DiscogsTrackDto> trackList,
            String artistsRelease) {

        List<DiscogsTrackListResponseDto> result = new ArrayList<>();

        if (!isEmpty(trackList)) {
            trackList.forEach(trackDto -> {
                DiscogsTrackListResponseDto dto = getTrackListDto(trackDto, artistsRelease);
                result.add(dto);
            });
        }

        return result;
    }

    private DiscogsTrackListResponseDto getTrackListDto(DiscogsTrackDto trackDto, String artistsRelease) {
        DiscogsTrackListResponseDto dto = new DiscogsTrackListResponseDto();

        dto.setTitle(trackDto.getTitle());
        dto.setPosition(trackDto.getPosition());

        String artists = !isEmpty(trackDto.getArtists()) ? concatArtists(trackDto.getArtists()) : artistsRelease;
        dto.setArtists(artists);

        return dto;
    }

}
