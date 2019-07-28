package songbox.house.service.impl;

import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.ResourceId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import songbox.house.client.YoutubeApiClient;
import songbox.house.domain.entity.YoutubePlaylist;
import songbox.house.domain.entity.YoutubePlaylistItem;
import songbox.house.service.YoutubeApiService;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class YoutubeApiServiceImpl implements YoutubeApiService {

    private static final Long PAGE_SIZE = 50L;

    YoutubeApiClient youtubeApiClient;

    @Override
    public List<YoutubePlaylist> getPlaylists() {
        final List<YoutubePlaylist> result = newArrayList();

        String nextPageToken = null;
        do {
            Optional<PlaylistListResponse> playlistsPage = youtubeApiClient.getPlaylists(PAGE_SIZE, nextPageToken);

            nextPageToken = playlistsPage
                    .map(PlaylistListResponse::getNextPageToken)
                    .orElse(null);

            result.addAll(playlistsPage
                    .map(this::convertToYoutubePlaylists)
                    .orElse(emptyList()));
        } while (nextPageToken != null);

        return result;
    }

    @Override
    public List<YoutubePlaylistItem> getItems(String playlistId) {
        final List<YoutubePlaylistItem> result = newArrayList();

        String nextPageToken = null;
        do {
            Optional<PlaylistItemListResponse> playlistItemsPage = youtubeApiClient.getPlaylistItems(playlistId, PAGE_SIZE, nextPageToken);

            nextPageToken = playlistItemsPage
                    .map(PlaylistItemListResponse::getNextPageToken)
                    .orElse(null);

            result.addAll(playlistItemsPage
                    .map(this::convertToTitleVideoDtos)
                    .orElse(emptyList()));
        } while (nextPageToken != null);

        return result;
    }

    private List<YoutubePlaylistItem> convertToTitleVideoDtos(PlaylistItemListResponse playlistItemListResponse) {
        return playlistItemListResponse.getItems().stream()
                .map(this::convertToYoutubePlaylistItem)
                .collect(toList());
    }

    private YoutubePlaylistItem convertToYoutubePlaylistItem(PlaylistItem playlistItem) {
        PlaylistItemSnippet snippet = playlistItem.getSnippet();
        String title = snippet.getTitle();

        ResourceId resourceId = snippet.getResourceId();
        String videoId = resourceId.getVideoId();

        return YoutubePlaylistItem.of(title, videoId);
    }

    private List<YoutubePlaylist> convertToYoutubePlaylists(PlaylistListResponse playlistListResponse) {
        return playlistListResponse.getItems().stream()
                .map(this::convertToYoutubePlayList)
                .collect(toList());
    }

    private YoutubePlaylist convertToYoutubePlayList(Playlist playlist) {
        String youtubePlaylistId = playlist.getId();

        PlaylistSnippet snippet = playlist.getSnippet();
        String title = snippet.getTitle();

        return new YoutubePlaylist(youtubePlaylistId, title);
    }
}
