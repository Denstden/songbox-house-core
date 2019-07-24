package songbox.house.service;

import songbox.house.domain.entity.Track;

public interface GoogleDriveService {
    void upload(Track track);
    void upload(String userName, Track track, String folder, String genreFolder);
}
