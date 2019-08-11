package songbox.house.client;

import org.jsoup.Connection.Response;

import java.util.Map;

public interface VkClient {
    Map<String, String> getCookies();

    Response searchFromMusic(Long ownerId, String searchQuery, String offset);

    Response searchFromNewsFeed(String searchQuery);

    Response reload(String audioIds);

    Response getContentLength(String url);

}
