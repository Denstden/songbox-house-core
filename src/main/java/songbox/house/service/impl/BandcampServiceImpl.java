package songbox.house.service.impl;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import songbox.house.client.BandcampClient;
import songbox.house.domain.dto.request.SearchRequestDto;
import songbox.house.domain.dto.response.SearchAndDownloadResponseDto;
import songbox.house.domain.dto.response.TrackDto;
import songbox.house.service.BandcampService;
import songbox.house.service.search.TrackDownloadService;
import songbox.house.util.ArtistsTitle;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static songbox.house.util.ArtistsTitle.parse;

@Service
@Data
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BandcampServiceImpl implements BandcampService {

    BandcampClient bandcampClient;
    TrackDownloadService trackDownloadService;

    @Override
    public SearchAndDownloadResponseDto searchAndDownload(String link, Long collectionId, Set<String> genres,
            boolean only320) {

        final SearchAndDownloadResponseDto searchAndDownloadResponseDto = new SearchAndDownloadResponseDto();
        try {
            final Response response = bandcampClient.search(link);
            Document document = response.parse();
            Element trackTable = document.select("#track_table").get(0);
            List<SearchRequestDto> tracks = trackTable.select(".track_row_view").stream()
                    .map(element -> parse(element.select(".track-title").text()))
                    .map(artistsTitle -> toSearchRequestDto(artistsTitle, collectionId, genres, only320))
                    .collect(toList());

            final List<TrackDto> result = tracks.stream()
                    .map(trackDownloadService::searchAndDownload)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toList());
            searchAndDownloadResponseDto.setFound(result);
            log.info("Downloaded {} of {} tracks", result.size(), tracks.size());
        } catch (Exception e) {
            log.error("Can't download from bandcamp by link {}", link, e);
        }

        return searchAndDownloadResponseDto;
    }

    private SearchRequestDto toSearchRequestDto(ArtistsTitle artistsTitle, Long collectionId, Set<String> genres,
            boolean only320) {
        final SearchRequestDto searchRequestDto = new SearchRequestDto();
        searchRequestDto.setCollectionId(collectionId);
        searchRequestDto.setTitle(artistsTitle.getTitle());
        searchRequestDto.setArtists(artistsTitle.getArtists());
        searchRequestDto.setOnly320(only320);
        searchRequestDto.setGenres(genres);
        return searchRequestDto;
    }
}
