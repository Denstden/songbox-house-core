package songbox.house.service;

import songbox.house.domain.dto.response.TrackDto;
import songbox.house.domain.entity.Track;

public interface GoogleDriveService {
    boolean upload(TrackDto track);
    boolean upload(TrackDto track, String folder, String genreFolder);
}
