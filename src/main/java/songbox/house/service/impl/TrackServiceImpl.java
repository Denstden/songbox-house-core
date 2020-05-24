package songbox.house.service.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songbox.house.converter.TrackConverter;
import songbox.house.domain.TrackSource;
import songbox.house.domain.dto.response.TracksDto;
import songbox.house.domain.entity.Author;
import songbox.house.domain.entity.Genre;
import songbox.house.domain.entity.MusicCollection;
import songbox.house.domain.entity.Track;
import songbox.house.domain.entity.TrackContent;
import songbox.house.exception.AccessDeniedException;
import songbox.house.exception.NotExistsException;
import songbox.house.repository.GenreRepository;
import songbox.house.repository.TrackRepository;
import songbox.house.service.AuthorService;
import songbox.house.service.MusicCollectionService;
import songbox.house.service.TrackService;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static java.util.Objects.isNull;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.util.CollectionUtils.isEmpty;
import static songbox.house.util.StringUtils.parseAuthors;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Service
@Transactional
@Slf4j
public class TrackServiceImpl implements TrackService {

    TrackRepository trackRepository;
    GenreRepository genreRepository;
    MusicCollectionService collectionService;
    AuthorService authorService;
    TrackConverter trackConverter;
    boolean saveToDBEnabled;

    public TrackServiceImpl(TrackRepository trackRepository, GenreRepository genreRepository,
            MusicCollectionService collectionService, AuthorService authorService,
            TrackConverter trackConverter,
            @Value("${songbox.house.vk.download.save_to_db.enabled:true}") boolean saveToDBEnabled) {
        this.trackRepository = trackRepository;
        this.genreRepository = genreRepository;
        this.collectionService = collectionService;
        this.authorService = authorService;
        this.trackConverter = trackConverter;
        this.saveToDBEnabled = saveToDBEnabled;
    }

    @Override
    @Transactional(propagation = REQUIRES_NEW)
    public Track create(final String title, final String fileName, final Set<Author> authors, final String extension,
            final byte[] content, final Double sizeMb, final Short bitRate, final TrackSource trackSource,
            final Set<String> genreNames, final MusicCollection collection) {

        final Set<Genre> genres = getGenres(genreNames);

        final Track track = new Track();
        track.setTitle(title.trim());
        track.setFileName(fileName.trim());
        track.setAuthors(authors);
        track.setExtension(extension);
        setContent(content, track);
        track.setBitRate(bitRate);
        track.setTrackSource(trackSource);
        track.setGenres(genres);
        track.setSizeMb(sizeMb);
        track.setCollections(new HashSet<MusicCollection>() {{
            add(collection);
        }});

        return trackRepository.save(track);
    }

    @Override
    @Transactional(propagation = REQUIRES_NEW)
    public Track save(final Track track, final Set<String> genres, final Long collectionId) {
        return save(track, genres, collectionId, null);
    }

    @Override
    @Transactional(propagation = REQUIRES_NEW)
    public Track save(Track track, Set<String> genres, Long collectionId, Long ownerId) {
        if (saveToDBEnabled) {
            if (isNotEmpty(track.getAuthorsStr()) && (isEmpty(track.getAuthors()))) {
                setAuthors(track.getAuthorsStr(), track);
            }

            track.setGenres(getGenres(genres));

            setCollections(track, collectionId, ownerId);

            return trackRepository.save(track);
        } else {
            return track;
        }
    }

    @Override
    public Track findByArtistAndTitle(final String artist, final String title) {
        return trackRepository.findFirstByAuthorsStrIgnoreCaseAndTitleIgnoreCase(artist, title);
    }

    @Override
    public Integer deleteAllTracks(final Long collectionId) {
        return trackRepository.deleteByCollections_CollectionId(collectionId);
    }

    @Override
    public TracksDto findAllByCollectionId(final Long collectionId, final Integer pageSize, final Integer pageNumber) {
        collectionService.checkOwner(collectionId);

        final Page<Track> tracks = trackRepository.findByCollections_CollectionId(collectionId, new PageRequest(pageNumber, pageSize));

        return trackConverter.toDto(tracks);
    }

    @Override
    public Track getById(final Long trackId) {
        final Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new NotExistsException(format("Track with id {0} not exists!", trackId)));

        boolean canGet = collectionService.checkCanGet(track.getCollections());

        if (!canGet) {
            throw new AccessDeniedException("User have not permissions to get track");
        }

        return track;
    }

    @Override
    public Iterable<Track> getByIds(Set<Long> trackIds) {
        return stream(trackRepository.findAllById(trackIds).spliterator(), false)
                .filter(track -> collectionService.checkCanGet(track.getCollections()))
                .collect(Collectors.toList());
    }

    @Override
    public void addToCollection(final Track fromDb, final Long collectionId) {
        if (!existsInCollection(fromDb.getCollections(), collectionId)) {
            final MusicCollection collection = collectionService.findById(collectionId);
            fromDb.getCollections().add(collection);

            trackRepository.save(fromDb);
        }
    }

    private void setContent(final byte[] content, final Track track) {
        final TrackContent trackContent = new TrackContent();
        trackContent.setContent(content);
        track.setContent(trackContent);
    }

    private boolean existsInCollection(final Set<MusicCollection> collections, final Long collectionId) {
        return collections.stream().anyMatch(collection -> collection.getCollectionId().equals(collectionId));
    }

    private void setAuthors(final String authorsStr, final Track track) {
        final Set<String> authorsString = parseAuthors(authorsStr);
        final Set<Author> authors = authorService.getOrCreateAuthors(authorsString);
        track.setAuthors(authors);
    }

    private Set<Genre> getGenres(final Set<String> genreNames) {
        final Set<Genre> genres = new HashSet<>();

        if (genreNames != null) {
            genreNames.forEach(genreName -> {
                final Genre genre = getOrCreate(genreName);
                genres.add(genre);
            });
        }

        return genres;
    }

    private Genre getOrCreate(final String genreName) {
        Genre genre = genreRepository.findByName(genreName);

        if (genre == null) {
            genre = new Genre(genreName);
        }
        return genre;
    }

    private void setCollections(final Track track, final Long collectionId, final Long ownerId) {
        final MusicCollection collection = isNull(ownerId)
                ? collectionService.findById(collectionId)
                : collectionService.findById(collectionId, ownerId);
        if (collection != null) {
            final Set<MusicCollection> collections = new HashSet<>();
            collections.add(collection);
            track.setCollections(collections);
        } else {
            log.warn("Collection with id {} not found.", collectionId);
        }
    }
}
