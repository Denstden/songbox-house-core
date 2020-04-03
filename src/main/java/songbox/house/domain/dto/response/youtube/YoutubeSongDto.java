package songbox.house.domain.dto.response.youtube;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import songbox.house.util.ArtistsTitle;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Data
public class YoutubeSongDto {
    ArtistsTitle artistsTitle;
    Integer duration;
    String thumbnail;
    String videoId;
}
