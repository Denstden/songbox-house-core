package songbox.house.util.parser;

import org.junit.Test;
import songbox.house.domain.dto.response.youtube.YoutubeSongDto;
import songbox.house.util.ResourceLoader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class YoutubeSearchParserTest {

    @Test
    public void shouldParseYoutube() throws IOException, URISyntaxException {
        String html = ResourceLoader.loadResource("youtube/search.html");

        List<YoutubeSongDto> youtubeSongDtos = YoutubeSearchParser.parseHtmlDocumentForSearch(html);

        assertEquals(16, youtubeSongDtos.size());
    }

    @Test
    public void shouldParseSize() throws IOException, URISyntaxException {
        String html = ResourceLoader.loadResource("youtube/320youtube.html");

        double size = YoutubeSearchParser.parseSizeMb(html).get();

        assertEquals(12.17d, size, 0.0001);
    }

    @Test
    public void shouldParseUrl() throws IOException, URISyntaxException {
        String html = ResourceLoader.loadResource("youtube/320youtube.html");

        String url = YoutubeSearchParser.parseMp3Url(html).get();

        assertEquals("https://s02.ytapivmp3.com/@download/251-5e8c80dc9e9bf-12760000-319-320-webm-5691624/mp3/QsE0STLkskk/Sync24%2B-%2BThis%2BLife.mp3", url);
    }

}