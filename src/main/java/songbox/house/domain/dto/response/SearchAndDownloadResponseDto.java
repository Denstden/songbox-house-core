package songbox.house.domain.dto.response;

import lombok.Data;
import songbox.house.domain.entity.Track;

import java.util.List;

@Data
public class SearchAndDownloadResponseDto {
    //TODO TrackDto
    private List<Track> found;
    private List<String> notFound;
}
