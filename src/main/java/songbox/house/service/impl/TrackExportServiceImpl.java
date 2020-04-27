package songbox.house.service.impl;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import songbox.house.domain.entity.Genre;
import songbox.house.domain.entity.Track;
import songbox.house.service.TrackExportService;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Set;

@Service
@Slf4j
public class TrackExportServiceImpl implements TrackExportService {

    @Override
    @SneakyThrows
    public void exportTrack(final Track track, final File collectionFolder,
            final boolean createCopyInEveryGenreFolder) {
        final Set<Genre> genres = track.getGenres();

        if (!CollectionUtils.isEmpty(genres)) {
            if (createCopyInEveryGenreFolder) {
                genres.forEach(genre -> createFileInGenreFolder(track, collectionFolder, genre.getName()));
            } else {
                final String mainGenre = track.getMainGenre();
                if (mainGenre != null) {
                    createFileInGenreFolder(track, collectionFolder, mainGenre);
                } else {
                    createFileInRootFolder(track, collectionFolder);
                }
            }
        } else {
            createFileIfNotExists(track, collectionFolder);
        }
    }

    @SneakyThrows
    private void createFileInGenreFolder(final Track track, final File collectionFolder, final String genreName) {
        final File genreFolder = new File(collectionFolder, genreName);
        if (!genreFolder.isDirectory()) {
            genreFolder.mkdir();
        }

        createFileIfNotExists(track, genreFolder);
    }

    @SneakyThrows
    private void createFileInRootFolder(final Track track, final File collectionFolder) {
        createFileIfNotExists(track, collectionFolder);
    }

    private void createFileIfNotExists(final Track track, final File folder) throws IOException {
        final FilenameFilter filenameFilter = new NameFileFilter(track.getFileName(), IOCase.INSENSITIVE);
        final String[] list = folder.list(filenameFilter);

        if (list != null && list.length > 0) {
            log.info("Track \"{}\" already exists in the folder \"{}\". Skipping export.", track.getFileName(), folder.getName());
        } else {
            final File trackFile = new File(folder, track.getFileName());
            final byte[] content = track.getContent().getContent();
            FileUtils.writeByteArrayToFile(trackFile, content);
        }
    }
}
