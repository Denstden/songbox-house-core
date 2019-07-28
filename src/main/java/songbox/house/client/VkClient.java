package songbox.house.client;

import org.jsoup.Connection.Response;

import java.util.Map;

public interface VkClient {

    void setCookies(Map<String, String> cookies);

    void addCookies(Map<String, String> cookies);

    void clearCookies();

    Response searchFromMusic(Long ownerId, String searchQuery, String offset);

    Response searchFromNewsFeed(String searchQuery);

    Response reload(String audioIds);

    Response getContentLength(String url);

}
