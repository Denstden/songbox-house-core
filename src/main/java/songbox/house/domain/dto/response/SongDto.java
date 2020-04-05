package songbox.house.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SongDto {
    private String artist;
    private String title;
    private Integer duration;
    private Short bitRate;
    private String thumbnail;
    private String uri;
    private String resource;
    private String trackPos; // A1
    private Double sizeMb;

    private Set<String> genres;
}
