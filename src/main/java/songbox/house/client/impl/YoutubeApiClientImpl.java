package songbox.house.client.impl;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import songbox.house.client.YoutubeApiClient;
import songbox.house.service.GoogleAuthenticationService;

import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Service
@Slf4j
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class YoutubeApiClientImpl implements YoutubeApiClient {

    private static final String SNIPPET = "snippet";

    GoogleAuthenticationService googleAuthenticationService;

    @Override
    public Optional<PlaylistListResponse> getPlaylists(Long count, String nextPageToken) {

        YouTube youTube = googleAuthenticationService.getYouTube();

        try {
            return of(youTube.playlists()
                    .list(SNIPPET)
                    .setMaxResults(count)
                    .setPageToken(nextPageToken)
                    .setMine(true)
                    .execute());
        } catch (IOException e) {
            log.error("Error getting playlists (count {}, nextPageToken {})", count, nextPageToken, e);
            return empty();
        }
    }

    @Override
    public Optional<PlaylistListResponse> getPlaylist(String playlistId) {

        YouTube youTube = googleAuthenticationService.getYouTube();

        try {
            return of(youTube.playlists()
                    .list(SNIPPET)
                    .setMaxResults(1L)
                    .setId(playlistId)
                    .execute());
        } catch (IOException e) {
            log.error("Error getting playlist by id {}", playlistId, e);
            return empty();
        }
    }

    @Override
    public Optional<PlaylistItemListResponse> getPlaylistItems(String playlistId, Long count, String nextPageToken) {

        YouTube youTube = googleAuthenticationService.getYouTube();

        try {
            return of(youTube.playlistItems()
                    .list(SNIPPET)
                    .setPlaylistId(playlistId)
                    .setMaxResults(count)
                    .setPageToken(nextPageToken)
                    .execute());
        } catch (IOException e) {
            log.error("Error getting play list item list (playlistId {}, count {}, nextPageToken {})", playlistId, count, nextPageToken, e);
            return empty();
        }
    }
}
