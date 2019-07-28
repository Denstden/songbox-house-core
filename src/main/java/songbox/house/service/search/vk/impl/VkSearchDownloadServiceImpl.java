package songbox.house.service.search.vk.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import songbox.house.domain.entity.SearchHistory;
import songbox.house.domain.entity.Track;
import songbox.house.domain.entity.VkAudio;
import songbox.house.service.TrackService;
import songbox.house.service.search.vk.VkAudioService;
import songbox.house.service.search.vk.VkDownloadService;
import songbox.house.service.search.vk.VkSearchDownloadService;
import songbox.house.service.search.vk.VkSearchService;

import java.util.Optional;
import java.util.Set;

import static java.util.Optional.of;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
@AllArgsConstructor
public class VkSearchDownloadServiceImpl implements VkSearchDownloadService {

    TrackService trackService;
    VkSearchService vkSearchService;
    VkDownloadService vkDownloadService;
    VkAudioService vkAudioService;

    @Override
    public Optional<Track> searchAndDownload(final String authors, final String title, final Set<String> genres,
            final Long collectionId, final SearchHistory searchHistory) {

        return vkSearchService.searchForDownloading(authors, title, searchHistory, false)
                .flatMap(audio -> download(audio, genres, collectionId));
    }

    private Optional<Track> download(final VkAudio audio, final Set<String> genres, final Long collectionId) {
        final Track fromDb = trackService.findByArtistAndTitle(audio.getArtist(), audio.getTitle());
        if (fromDb != null) {
            log.debug("Found track in db, not perform searching.");
            trackService.addToCollection(fromDb, collectionId);
            // TODO downloadHistory
            return of(fromDb);
        }
        return saveVkAudioAndDownloadTrack(audio, genres, collectionId);
    }

    @Override
    public void searchAndDownloadAsync(final String authors, final String title, final Set<String> genres,
            final Long collectionId, final SearchHistory searchHistory, boolean only320) {

        final Optional<VkAudio> vkAudio = vkSearchService.searchForDownloading(authors, title, searchHistory, only320);

        if (vkAudio.isPresent()) {
            final VkAudio audio = vkAudio.get();

            final Track fromDb = trackService.findByArtistAndTitle(audio.getArtist(), audio.getTitle());
            if (fromDb != null) {
                log.debug("Found track in db, not perform searching.");
                trackService.addToCollection(fromDb, collectionId);
                // TODO downloadHistory
            } else {
                vkAudioService.save(audio);
                vkDownloadService.downloadAsync(audio, genres, collectionId);
            }
        } else {
            // TODO downloadHistory
        }
    }

    private Optional<Track> saveVkAudioAndDownloadTrack(final VkAudio audio, final Set<String> genres,
            final Long collectionId) {
        vkAudioService.save(audio);
        return vkDownloadService.download(audio, genres, collectionId);
    }
}
