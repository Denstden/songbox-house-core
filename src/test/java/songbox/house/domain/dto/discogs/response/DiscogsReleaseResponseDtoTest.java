package songbox.house.domain.dto.discogs.response;

import org.junit.Test;
import songbox.house.domain.dto.response.discogs.DiscogsReleaseResponseDto;
import songbox.house.util.JsonUtils;
import songbox.house.util.ResourceLoader;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class DiscogsReleaseResponseDtoTest {
    @Test
    public void shouldDeserialize() throws IOException, URISyntaxException {
        final String content = ResourceLoader.loadResource("discogs/release.json");

        final DiscogsReleaseResponseDto release = JsonUtils.fromString(content, DiscogsReleaseResponseDto.class);

        assertNotNull(release);
    }
}