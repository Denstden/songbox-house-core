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

}