package songbox.house.service.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import songbox.house.domain.FileNameData;
import songbox.house.domain.TrackSource;
import songbox.house.domain.entity.Author;
import songbox.house.domain.entity.MusicCollection;
import songbox.house.domain.entity.Track;
import songbox.house.service.AuthorService;
import songbox.house.service.FileSystemImportService;
import songbox.house.service.MusicCollectionService;
import songbox.house.service.TrackService;
import songbox.house.util.parser.FileNameParser;

import javax.persistence.EntityManager;
import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
/*
  Accepted files hierarchy : root/genre/track1.mp3 or root/genre/genre/track1.mp3
 */
public class DenstFileSystemImportServiceImpl implements FileSystemImportService {
    EntityManager entityManager;

    MusicCollectionService musicCollectionService;
    AuthorService authorService;

    TrackService trackService;
    FileNameParser fileNameParser;

    @Override
    public Integer importCollectionFromFileSystem(final String collectionName, final String root, final Short bitRate) {
        MusicCollection collection = musicCollectionService.findByName(collectionName);

        if (collection == null) {
            collection = new MusicCollection(collectionName);
            musicCollectionService.save(collection);
        }

        final File rootFile = new File(root);

        if (rootFile.isDirectory()) {
            return syncRoot(rootFile, bitRate, collection);
        }

        return 0;
    }

    private Integer syncRoot(final File root, final Short bitRate, final MusicCollection collection) {
        Integer synced = 0;

        final File[] genres = root.listFiles();

        if (genres != null) {
            for (final File genreFolder : genres) {
                if (genreFolder.isDirectory()) {
                    synced += syncGenre(bitRate, genreFolder, collection);
                }
            }
        }

        return synced;
    }

    private Integer syncGenre(final Short bitRate, final File genreFolder, final MusicCollection collection) {
        Integer synced = 0;

        final File[] tracks = genreFolder.listFiles();
        if (tracks != null) {
            for (final File file : tracks) {
                final String genre = getGenreName(genreFolder);
                if (file.isDirectory()) {
                    synced += processGenreSubFolder(bitRate, genre, file, collection);
                } else {
                    if (syncTrack(bitRate, genre, file, collection)) {
                        synced++;
                    }
                }
            }
        }

        return synced;
    }

    private String getGenreName(final File genreFolder) {
        final String folderName = genreFolder.getName();
        return Character.toUpperCase(folderName.charAt(0)) + folderName.substring(1).toLowerCase();
    }

    private Integer processGenreSubFolder(final Short bitRate, final String genre, final File file,
            final MusicCollection collection) {
        Integer synced = 0;

        final File[] tracks = file.listFiles();
        if (tracks != null) {
            for (final File track : tracks) {
                if (syncTrack(bitRate, genre, track, collection)) {
                    synced++;
                }
            }
        }

        return synced;
    }

    private boolean syncTrack(final Short bitRate, final String genre, final File file,
            final MusicCollection collection) {
        final Optional<Track> savedTrack = saveTrack(bitRate, file, genre, collection);

        //to prevent out of memory error
        savedTrack.ifPresent(entityManager::detach);

        return savedTrack.isPresent();
    }

    private Optional<Track> saveTrack(final Short bitRate, final File track, final String genre,
            final MusicCollection collection) {
        try {
            final String fileName = track.getName();
            final FileNameData fileNameData = fileNameParser.parseFileName(fileName);
            final String title = fileNameData.getTitle();
            final Set<Author> authors = authorService.getOrCreateAuthors(fileNameData.getAuthors());
            final String extension = fileNameData.getExtension();
            final byte[] content = Files.readAllBytes(track.toPath());
            final Set<String> genres = new HashSet<String>() {{
                add(genre);
            }};

            return Optional.of(trackService.create(title, fileName, authors, extension, content, track.length() / 1024 / 1024., bitRate, TrackSource.FILESYSTEM, genres, collection));
        } catch (final Exception e) {
            return Optional.empty();
        }
    }


}
