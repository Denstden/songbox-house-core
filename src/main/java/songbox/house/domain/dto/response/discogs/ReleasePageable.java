package songbox.house.domain.dto.response.discogs;

import java.util.List;

public interface ReleasePageable {
    DiscogsPaginationDto getPagination();

    List<DiscogsReleaseDto> getReleases();
}
