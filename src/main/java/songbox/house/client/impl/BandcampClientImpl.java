package songbox.house.client.impl;

import org.jsoup.Connection.Response;
import org.springframework.stereotype.Component;
import songbox.house.client.BandcampClient;

import java.io.IOException;

import static org.jsoup.Connection.Method.GET;
import static org.jsoup.Jsoup.connect;

@Component
public class BandcampClientImpl implements BandcampClient {
    @Override
    public Response search(String searchQuery) throws IOException {
        return connect(searchQuery)
                .method(GET)
                .execute();
    }
}
