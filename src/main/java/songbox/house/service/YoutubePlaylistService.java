package songbox.house.service;

import songbox.house.domain.entity.YoutubePlaylist;
import songbox.house.domain.entity.YoutubePlaylistItem;

import java.util.List;

public interface YoutubePlaylistService {

    YoutubePlaylist getByYoutubeId(String youtubePlaylistId);

    List<YoutubePlaylist> loadPlaylists();

    List<YoutubePlaylistItem> loadPlaylistItems(String youtubePlaylistId);

    void sync(String playlistId, String collectionName, boolean only320);
}
