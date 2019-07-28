package songbox.house.domain.dto.request;

import lombok.Data;

@Data
public class ArtistTitleDto {
    private final String artist;
    private final String title;

    @Override
    public String toString() {
        return artist + " - " + title;
    }
}
