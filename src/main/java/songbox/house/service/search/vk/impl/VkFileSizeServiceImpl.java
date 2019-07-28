package songbox.house.service.search.vk.impl;

import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection.Response;
import org.springframework.stereotype.Service;
import songbox.house.client.VkClient;
import songbox.house.service.search.vk.VkFileSizeService;

import static java.lang.Long.valueOf;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Service
@FieldDefaults(makeFinal = true, level = PRIVATE)
@AllArgsConstructor
public class VkFileSizeServiceImpl implements VkFileSizeService {

    private static final String CONTENT_RANGE_HEADER = "content-range";

    VkClient vkClient;

    @Override
    public long getSizeBytes(String url) {
        final String sizeHeader = getSizeHeader(url);
        final String size = sizeHeader.substring(10);
        log.trace("FileSize: {} - {}", size, url);
        return valueOf(size);
    }

    @Override
    public String getSizeHeader(String url) {
        final Response response = vkClient.getContentLength(url);
        return response.header(CONTENT_RANGE_HEADER);
    }
}
