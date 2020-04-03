package songbox.house.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import songbox.house.client.DiscogsClient;
import songbox.house.domain.dto.request.ArtistTitleDto;
import songbox.house.domain.dto.response.SongDto;
import songbox.house.domain.dto.response.discogs.DiscogsReleaseDtoExt;
import songbox.house.service.DiscogsWebsiteService;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class DiscogsWebsiteServiceImpl implements DiscogsWebsiteService {
    private static final String IMAGE_SELECTOR = ".thumbnail_center img";
    private DiscogsClient discogsClient;

    @Override
    public Optional<String> searchArtwork(String searchQuery) {
        log.trace("Looking for an artwork for query = {}", searchQuery);

        Response response = discogsClient.searchArtwork(searchQuery);
        try {
            if (response != null) {
                Document document = response.parse();
                Elements elements = document.select("#search_results " + IMAGE_SELECTOR);
                return elements.stream()
                        .map(e -> e.attr("data-src"))
                        .filter(e -> !e.isEmpty())
                        .findFirst();
            }
        } catch (Exception e) {
            log.error("Error downloading artworks", e);

        }
        return Optional.empty();
    }

    @Override
    public List<DiscogsReleaseDtoExt> search(String query) {
        Response response = discogsClient.search(query);

        ArrayList<DiscogsReleaseDtoExt> result = new ArrayList<>();

        try {
            if (response != null) {
                Document document = response.parse();
                Elements elements = document.select("#search_results .card");
                for (int i = 0; i < elements.size(); i++) {
                    try {
                        Element element = elements.get(i);

                        Elements searchResultElement = element.select(".search_result_title");

                        String artist = element.select("a").stream()
                                .filter(e -> e.attr("href").startsWith("/artist"))
                                .map(Element::text)
                                .findFirst().orElse("");
                        String album = searchResultElement.text();
                        String artwork = element.select(IMAGE_SELECTOR).stream()
                                .map(e -> e.attr("data-src"))
                                .findFirst().orElse("");
                        String releaseLink = searchResultElement.attr("href");

                        DiscogsReleaseDtoExt discogsReleaseDtoExt = new DiscogsReleaseDtoExt();

                        discogsReleaseDtoExt.setThumbnail(artwork);
                        discogsReleaseDtoExt.setArtistTitle(new ArtistTitleDto(artist, album));
                        discogsReleaseDtoExt.setDiscogsLink(releaseLink);
                        result.add(discogsReleaseDtoExt);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }

        return result;
    }

    @Override
    @Nullable
    public Optional<DiscogsReleaseDtoExt> getReleaseInfo(String discogsLink) {
        Response response = discogsClient.getReleaseByLink(discogsLink);

        try {
            if (response != null) {
                DiscogsReleaseDtoExt discogsReleaseDtoExt = new DiscogsReleaseDtoExt();
                Map<ArtistTitleDto, List<SongDto>> songDtos = new HashMap<>();
                Document document = response.parse();
                applyProfile(discogsReleaseDtoExt, document);

                Elements trackList = document.select(".tracklist_track");
                for (int i = 0; i < trackList.size(); i++) {
                    Element trackElement = trackList.get(i);

                    String artist = trackElement.select(".tracklist_track_artists").text().trim();
                    if (artist.isEmpty()) {
                        artist = discogsReleaseDtoExt.getArtistTitle().getArtist();
                    }
                    artist = removeInfoCounter(artist);

                    String trackTitle = "";
                    Element trackTitleElement = trackElement.select(".tracklist_track_title").first();
                    if (trackTitleElement != null) {
                        trackTitle = trackTitleElement.text().trim();
                    }

                    SongDto songDto = new SongDto();
                    songDto.setArtist(replaceUnicodeCharacters(artist).replaceAll("^-", ""));
                    songDto.setTitle(trackTitle);
                    songDto.setDuration(parseTrackDuration(trackElement));
                    songDto.setTrackPos(trackElement.select(".tracklist_track_pos").text().trim());
                    songDtos.put(new ArtistTitleDto(artist, trackTitle), Arrays.asList(songDto));
                }

                discogsReleaseDtoExt.setSongs(songDtos);
                discogsReleaseDtoExt.setDiscogsLink(discogsLink);
                discogsReleaseDtoExt.setThumbnail(document.select(".body img").attr("src"));
                return Optional.of(discogsReleaseDtoExt);
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return Optional.empty();
    }

    /**
     * Some of the artist contains count at the end of name -> Vid(3)
     *
     * @param string artist, label
     * @return refactored string
     */
    private String removeInfoCounter(String string) {
        if (string == null) {
            return null;
        }
        return string.trim().replaceAll("\\(\\d+?\\)$", "").trim();
    }

    private Integer parseTrackDuration(Element trackElement) {
        Elements elements = trackElement.select(".tracklist_track_duration");
        String[] duration = elements.text().split(":");
        if (duration.length == 2) {
            try {
                return 60 * Integer.parseInt(duration[0]) + Integer.parseInt(duration[1]);
            } catch (NumberFormatException ignored) {
            }
        }

        return 0;
    }

    private void applyProfile(DiscogsReleaseDtoExt discogsReleaseDtoExt, Document document) {
        // Apply artist title
        String artistTitleString = replaceUnicodeCharacters(document.select("#profile_title").text());
        String[] artistTitle = artistTitleString.split("-");

        String artist = removeInfoCounter(artistTitle.length > 0 ? artistTitle[0].trim() : "");
        String title = artistTitle.length > 1 ? artistTitle[1].trim() : "";

        ArtistTitleDto artistTitleDto = new ArtistTitleDto(artist, title);
        discogsReleaseDtoExt.setArtistTitle(artistTitleDto);

        // Apply label, county, style
        Elements profileSection = document.select(".profile div");
        for (int i = 0; i < profileSection.size(); i++) {
            Element element = profileSection.get(i);
            if (element.text().startsWith("Label") && i + 1 < profileSection.size()) {
                String text = profileSection.get(++i).text();
                String[] labelAndLabelRelease = replaceUnicodeCharacters(text).split("-");
                if (labelAndLabelRelease.length > 0) {
                    discogsReleaseDtoExt.setAudioLabel(replaceUnicodeCharacters(labelAndLabelRelease[0]).trim());
                } else {
                    discogsReleaseDtoExt.setAudioLabelReleaseName("");
                }

                if (labelAndLabelRelease.length > 1) {
                    discogsReleaseDtoExt.setAudioLabelReleaseName(replaceUnicodeCharacters(labelAndLabelRelease[1]).trim());
                } else {
                    discogsReleaseDtoExt.setAudioLabelReleaseName("");
                }
                discogsReleaseDtoExt.setAudioLabel(removeInfoCounter(discogsReleaseDtoExt.getAudioLabel()));
            } else if (element.text().startsWith("Country") && i + 1 < profileSection.size()) {
                discogsReleaseDtoExt.setCountry(profileSection.get(++i).text().trim());
            } else if (element.text().startsWith("Style") && i + 1 < profileSection.size()) {
                String[] styles = profileSection.get(++i).text().split(",");
                discogsReleaseDtoExt.setGenres(Arrays.asList(styles).stream().map(String::trim).collect(Collectors.toSet()));
            }
        }
    }

    private String replaceUnicodeCharacters(String s) {
        return s.replace("\u200E", " ")
                .replace("–" /*unicode dash*/, "-");
    }
}
