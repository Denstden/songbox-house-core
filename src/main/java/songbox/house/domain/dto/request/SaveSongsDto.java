package songbox.house.domain.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import songbox.house.domain.dto.response.SongDto;

import java.util.Set;

@Data
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SaveSongsDto {
    Long collectionId;
    Set<SongDto> songs;
}
