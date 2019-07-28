package songbox.house.service.search.youtube;

import songbox.house.service.search.SearchService;

public interface YoutubeSearchService extends SearchService {
    default String resourceName() {
        return "Youtube";
    }
}
