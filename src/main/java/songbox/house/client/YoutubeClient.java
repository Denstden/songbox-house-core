package songbox.house.client;

import org.jsoup.Connection;

import java.io.IOException;

public interface YoutubeClient {

    Connection.Response search(String searchQuery) throws IOException;
}
