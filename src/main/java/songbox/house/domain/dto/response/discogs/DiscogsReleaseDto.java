package songbox.house.domain.dto.response.discogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscogsReleaseDto {
    private String status;
    private String thumb;
    private String format;
    private String artist;
    private String catno;
    private Integer year;
    private String title;
    @JsonProperty("resource_url")
    private String resourceUri;
    private Long id;
}

//{
//        "status": "Accepted",
//        "thumb": "https://img.discogs.com/SA1hpKiw8q8pZNFnjuh0xZ_8tuY=/fit-in/150x150/filters:strip_icc():format(jpeg):mode_rgb():quality(40)/discogs-images/R-13341579-1552405266-5180.jpeg.jpg",
//        "format": "File, WAV, EP",
//        "artist": "Various",
//        "catno": "EJ 001 ",
//        "year": 2019,
//        "title": "Elevated Jit Vol 1",
//        "resource_url": "https://api.discogs.com/releases/13341579",
//        "id": 13341579
//        }
