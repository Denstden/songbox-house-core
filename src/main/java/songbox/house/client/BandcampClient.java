package songbox.house.client;

import org.jsoup.Connection.Response;

import java.io.IOException;

public interface BandcampClient {
    Response search(String searchQuery) throws IOException;
}
