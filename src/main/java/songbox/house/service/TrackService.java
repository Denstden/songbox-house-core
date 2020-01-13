package songbox.house.service;

import org.springframework.context.event.EventListener;
import songbox.house.domain.TrackSource;
import songbox.house.domain.dto.request.SaveSongsDto;
import songbox.house.domain.dto.response.TrackMetadataDto;
import songbox.house.domain.dto.response.TracksDto;
import songbox.house.domain.entity.Author;
import songbox.house.domain.entity.MusicCollection;
import songbox.house.domain.entity.Track;
import songbox.house.domain.event.vk.VkDownloadSuccessEvent;

import java.util.Set;

public interface TrackService {
    Track create(final String title, final String fileName, final Set<Author> authors, final String extension,
            final byte[] content, final Double sizeMb, final Short bitRate, final TrackSource trackSource,
            final Set<String> genres, final MusicCollection collection);

    Track save(final Track track, final Set<String> genres, final Long collectionId);

    Track findByArtistAndTitle(final String artist, final String title);

    Integer deleteAllTracks(final Long collectionId);

    TracksDto findAllByCollectionId(final Long collectionId, final Integer pageSize, final Integer pageNumber);

    Track getById(final Long trackId);

    Iterable<Track> getByIds(final Set<Long> trackIds);

    void addToCollection(Track fromDb, Long collectionId);

    @EventListener
    void onDownloadSuccessEvent(VkDownloadSuccessEvent event);

    Iterable<Track> download(SaveSongsDto saveSongsDto);

    Track download(TrackMetadataDto trackMetadataDto, Long collectionId);
}
