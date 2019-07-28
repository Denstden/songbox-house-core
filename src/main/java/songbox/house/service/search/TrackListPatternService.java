package songbox.house.service.search;

import songbox.house.domain.entity.TrackListPattern;

public interface TrackListPatternService {
    TrackListPattern create(String pattern, String example);
}
