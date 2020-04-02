package songbox.house.domain.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class SearchAndDownloadResponseDto {
    private List<TrackDto> found;
    private List<String> notFound;
}
