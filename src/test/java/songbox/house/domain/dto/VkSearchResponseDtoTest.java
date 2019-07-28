package songbox.house.domain.dto;

import org.junit.Test;
import songbox.house.domain.dto.response.vk.VkSearchResponseDto;
import songbox.house.util.JsonUtils;
import songbox.house.util.ResourceLoader;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertNotNull;

public class VkSearchResponseDtoTest {

    @Test
    public void shouldDeserialize() throws IOException, URISyntaxException {
        final String content = ResourceLoader.loadResource("vk/search.json");

        final VkSearchResponseDto vkSearchResponseDto = JsonUtils.fromString(content, VkSearchResponseDto.class);

        //TODO all fields
        assertNotNull(vkSearchResponseDto);
    }
}