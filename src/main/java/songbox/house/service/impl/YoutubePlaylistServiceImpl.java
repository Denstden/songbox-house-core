package songbox.house.service.impl;

import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.common.collect.Sets.SetView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import songbox.house.client.YoutubeApiClient;
import songbox.house.domain.dto.request.SearchRequestDto;
import songbox.house.domain.entity.MusicCollection;
import songbox.house.domain.entity.YoutubePlaylist;
import songbox.house.domain.entity.YoutubePlaylistItem;
import songbox.house.domain.entity.user.UserInfo;
import songbox.house.repository.YoutubePlaylistItemRepository;
import songbox.house.repository.YoutubePlaylistRepository;
import songbox.house.service.MusicCollectionService;
import songbox.house.service.UserService;
import songbox.house.service.YoutubeApiService;
import songbox.house.service.YoutubePlaylistService;
import songbox.house.service.search.TrackDownloadService;
import songbox.house.util.ArtistsTitle;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.collect.Sets.difference;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class YoutubePlaylistServiceImpl implements YoutubePlaylistService {

    YoutubeApiClient youtubeApiClient;
    YoutubePlaylistRepository youtubePlaylistRepository;
    YoutubePlaylistItemRepository youtubePlaylistItemRepository;
    YoutubeApiService youtubeApiService;
    UserService userService;
    MusicCollectionService musicCollectionService;
    TrackDownloadService trackDownloadService;

    @Override
    public YoutubePlaylist getByYoutubeId(String youtubePlaylistId) {
        return youtubePlaylistRepository.findByYoutubeId(youtubePlaylistId);
    }

    @Override
    public List<YoutubePlaylist> loadPlaylists() {
        List<YoutubePlaylist> playlists = youtubeApiService.getPlaylists();

        savePlaylists(playlists);

        return playlists;
    }

    @Override
    public List<YoutubePlaylistItem> loadPlaylistItems(String youtubePlaylistId) {
        List<YoutubePlaylistItem> items = youtubeApiService.getItems(youtubePlaylistId);

        //TODO return from db too
        saveItems(items, youtubePlaylistId);

        return items;
    }

    @Override
    public void sync(String playlistId, String collectionName, boolean only320) {
        List<YoutubePlaylistItem> items = youtubeApiService.getItems(playlistId);

        items.stream()
                .map(YoutubePlaylistItem::getTitle)
                .map(youtubeTitle -> createSearchRequest(youtubeTitle, collectionName, only320))
                .forEach(trackDownloadService::searchAndDownloadAsync);
    }

    private SearchRequestDto createSearchRequest(String youtubeTitle, String collectionName, boolean only320) {
        MusicCollection musicCollection = musicCollectionService.getOrCreate(collectionName);

        ArtistsTitle artistsTitle = ArtistsTitle.parse(youtubeTitle);

        SearchRequestDto searchRequest = new SearchRequestDto();
        searchRequest.setOnly320(only320);
        searchRequest.setArtists(artistsTitle.getArtists());
        searchRequest.setTitle(artistsTitle.getTitle());
        searchRequest.setCollectionId(musicCollection.getCollectionId());
        return searchRequest;
    }

    private void savePlaylists(List<YoutubePlaylist> playlists) {
        List<YoutubePlaylist> playlistsToSave = getYoutubeItemsToSave(playlists, YoutubePlaylist::getYoutubeId,
                youtubePlaylistRepository::findByYoutubeIdIn);

        setOwner(playlistsToSave);

        youtubePlaylistRepository.saveAll(playlistsToSave);
    }

    private void saveItems(List<YoutubePlaylistItem> items, String youtubePlaylistId) {
        List<YoutubePlaylistItem> playlistItemsToSave = getYoutubeItemsToSave(items, YoutubePlaylistItem::getYoutubeVideoId,
                youtubePlaylistItemRepository::findByYoutubeVideoIdIn);

        setPlaylist(youtubePlaylistId, playlistItemsToSave);

        youtubePlaylistItemRepository.saveAll(playlistItemsToSave);
    }

    private <T> List<T> getYoutubeItemsToSave(List<T> items, Function<T, String> getYoutubeId,
            Function<Set<String>, List<T>> getSavedItems) {

        Set<String> ids = getYoutubeVideoIds(items, getYoutubeId);

        List<T> itemsFromDb = getSavedItems.apply(ids);
        Set<String> saved = getYoutubeVideoIds(itemsFromDb, getYoutubeId);

        SetView<String> toSave = difference(ids, saved);

        return items.stream()
                .filter(t -> toSave.contains(getYoutubeId.apply(t)))
                .collect(toList());
    }

    private <T> Set<String> getYoutubeVideoIds(List<T> items, Function<T, String> getYoutubeId) {
        return items.stream()
                .map(getYoutubeId)
                .collect(toSet());
    }

    private void setOwner(List<YoutubePlaylist> playlistsToSave) {
        UserInfo currentUser = userService.getCurrentUser();
        playlistsToSave.forEach(playlist -> playlist.setOwner(currentUser));
    }


    private void setPlaylist(String youtubePlaylistId, List<YoutubePlaylistItem> playlistItemsToSave) {
        YoutubePlaylist playlist = getOrCrateByYoutubePlaylistId(youtubePlaylistId);
        playlistItemsToSave.forEach(item -> item.setPlaylist(playlist));
    }

    private YoutubePlaylist getOrCrateByYoutubePlaylistId(String youtubePlaylistId) {
        return ofNullable(getByYoutubeId(youtubePlaylistId))
                .orElseGet(() -> createYoutubePlaylist(youtubePlaylistId));
    }

    private YoutubePlaylist createYoutubePlaylist(String youtubePlaylistId) {
        String title = youtubeApiClient.getPlaylist(youtubePlaylistId)
                .map(this::getTitle)
                .orElse(null);

        return new YoutubePlaylist(youtubePlaylistId, title, userService.getCurrentUser());
    }

    private String getTitle(PlaylistListResponse playlistListResponse) {
        List<Playlist> items = playlistListResponse.getItems();

        if (items.size() > 0) {
            return items.get(0).getSnippet().getTitle();
        } else {
            return null;
        }
    }
}
