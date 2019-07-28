package songbox.house.domain.dto.response.youtube;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Data
public class YoutubeSongDto {
    String artist;
    String title;
    Integer duration;
    String thumbnail;
    String videoId;
}
