package songbox.house.service;

import songbox.house.domain.dto.response.TrackDto;
import songbox.house.domain.entity.Track;

public interface GoogleDriveService {
    void upload(TrackDto track);
    void upload(TrackDto track, String folder, String genreFolder);
}
