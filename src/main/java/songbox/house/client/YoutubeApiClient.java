package songbox.house.client;

import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;

import java.util.Optional;

public interface YoutubeApiClient {
    Optional<PlaylistListResponse> getPlaylists(Long count, String nextPageToken);

    Optional<PlaylistListResponse> getPlaylist(String playlistId);

    Optional<PlaylistItemListResponse> getPlaylistItems(String playlistId, Long count, String nextPageToken);
}
