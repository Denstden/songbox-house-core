package songbox.house.service;

import songbox.house.domain.entity.Track;

import java.io.File;

public interface TrackExportService {
    void exportTrack(final Track track, final File collectionFolder, final boolean createCopyInEveryGenreFolder);
}
