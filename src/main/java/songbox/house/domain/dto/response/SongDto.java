package songbox.house.domain.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.Set;

@Getter
@Setter
public class SongDto {
    private String artist;
    private String title;
    private Integer duration;
    private Short bitRate;
    private String thumbnail;
    private String uri;
    private String resource;
    private String trackPos; // A1

    private Set<String> genres;

    public SongDto() {
    }

    public SongDto(String artist, String title, Integer duration, Short bitRate, String thumbnail, URI uri,
            String resource) {
        this.artist = artist;
        this.title = title;
        this.duration = duration;
        this.bitRate = bitRate;
        this.thumbnail = thumbnail;
        this.uri = uri.toString();
        this.resource = resource;
    }

    public SongDto(String artist, String title, Integer duration, Short bitRate, String thumbnail, URI uri,
            String resource, Set<String> genres) {
        this.artist = artist;
        this.title = title;
        this.duration = duration;
        this.bitRate = bitRate;
        this.thumbnail = thumbnail;
        this.uri = uri.toString();
        this.resource = resource;
        this.genres = genres;
    }

    public SongDto(TrackMetadataDto t) {
        this(t.getArtists(), t.getTitle(), t.getDurationSec(), t.getBitRate(), t.getThumbnail(), URI.create(t.getUri()), t.getResource());
    }
}
