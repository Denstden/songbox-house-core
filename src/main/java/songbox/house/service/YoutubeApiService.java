package songbox.house.service;

import songbox.house.domain.entity.YoutubePlaylist;
import songbox.house.domain.entity.YoutubePlaylistItem;

import java.util.List;

public interface YoutubeApiService {
    List<YoutubePlaylist> getPlaylists();

    List<YoutubePlaylistItem> getItems(String playlistId);
}
