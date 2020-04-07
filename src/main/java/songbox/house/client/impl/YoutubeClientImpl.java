package songbox.house.client.impl;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import songbox.house.client.YoutubeClient;

import java.io.IOException;

@Slf4j
@Component
public class YoutubeClientImpl implements YoutubeClient {
    private static final String SEARCH_URL = "https://www.youtube.com/results";

    @Override
    public Connection.Response search(String searchQuery) throws IOException {
        return Jsoup.connect(SEARCH_URL)
                .data("search_query", searchQuery)
                .method(Connection.Method.GET)
                .execute();
    }

    @Override
    public Connection.Response getTrackMetadata(String videoId) throws IOException {
        return Jsoup.connect("https://www.320youtube.com" + videoId)
                .method(Connection.Method.GET)
                .execute();
    }
}
