package songbox.house.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import songbox.house.domain.entity.MusicCollection;
import songbox.house.domain.entity.Track;
import songbox.house.exception.NotExistsException;
import songbox.house.repository.TrackRepository;
import songbox.house.service.FileSystemExportService;
import songbox.house.service.MusicCollectionService;
import songbox.house.service.TrackExportService;

import javax.persistence.EntityManager;
import java.io.File;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
@Slf4j
public class DenstFileSystemExportServiceImpl implements FileSystemExportService {
    private String rootFolder = Paths.get(System.getProperty("user.dir"), "downloaded").toString();

    private final TrackRepository trackRepository;
    private final TrackExportService trackExportService;
    private final MusicCollectionService collectionService;
    private final EntityManager entityManager;

    @Autowired
    public DenstFileSystemExportServiceImpl(final TrackRepository trackRepository,
            final TrackExportService trackExportService,
            final MusicCollectionService collectionService, final EntityManager entityManager) {
        this.trackRepository = trackRepository;
        this.trackExportService = trackExportService;
        this.collectionService = collectionService;
        this.entityManager = entityManager;
    }

    @Override
    public Integer exportCollection(final String collectionName, final boolean createCopyInEveryGenre, boolean only320) {
        final MusicCollection collection = collectionService.findByName(collectionName);

        if (collection == null) {
            throw new NotExistsException("Collection with name \"" + collectionName + "\" not exists.");
        }

        final File root = new File(rootFolder);

        if (!root.exists() && !root.mkdirs()) {
            throw new IllegalStateException("Can not create destination folder.");
        }

        final File collectionFolder = new File(root, collectionName);
        collectionFolder.mkdir();

        Integer countExported = 0;

        final Page<Track> firstPage = trackRepository.findByCollections_CollectionId(collection.getCollectionId(), new PageRequest(0, 20));
        countExported += exportPage(firstPage, collectionFolder, createCopyInEveryGenre, only320);
        final int totalPages = firstPage.getTotalPages();
        for (int i = 1; i < totalPages; i++) {
            final Page<Track> page = trackRepository.findByCollections_CollectionId(collection.getCollectionId(), new PageRequest(i, 20));
            countExported += exportPage(page, collectionFolder, createCopyInEveryGenre, only320);
            log.info("Export of {} page of {} finished", i, totalPages);
        }

        log.info("Export of {} files finished. Folder {}", countExported, collectionFolder);

        return countExported;
    }

    private Integer exportPage(final Page<Track> page, final File collectionFolder,
                               final boolean createCopyInEveryGenre, boolean only320) {
        Integer exported = 0;

        for (final Track track : page) {
            if (track != null && (!only320 || track.getBitRate() != null && track.getBitRate() == 320)) {
                log.info("Exporting track {}", track.getFileName());
                trackExportService.exportTrack(track, collectionFolder, createCopyInEveryGenre);
                exported++;
                entityManager.detach(track);
            }
        }

        return exported;
    }
}
