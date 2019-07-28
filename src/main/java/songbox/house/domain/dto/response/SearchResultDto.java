package songbox.house.domain.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SearchResultDto {
    List<SongDto> songs;
}
