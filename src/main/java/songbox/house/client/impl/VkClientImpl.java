package songbox.house.client.impl;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import songbox.house.client.VkClient;
import songbox.house.util.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.System.currentTimeMillis;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.jsoup.Connection.Method.HEAD;
import static org.jsoup.Connection.Method.POST;
import static org.jsoup.Jsoup.connect;
import static songbox.house.util.Constants.PERFORMANCE_MARKER;
import static songbox.house.util.RetryUtil.getOptionalWithRetries;

@Slf4j
@Component
public class VkClientImpl implements VkClient {
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
    private static final String PATH_BASE = "https://vk.com";

    private Configuration configuration;

    private final Map<String, String> cookies = new HashMap<>();

    @Autowired
    public VkClientImpl(Configuration configuration) {
        this.configuration = configuration;
    }

    @PostConstruct
    public void defaultCookies() {
        final Map<String, String> cookies = new HashMap<>();

        String[] cookiesArray = configuration.getConnection().getVkCookie().split(";");
        for (String cookie : cookiesArray) {
            String[] split = cookie.split("=");
            cookies.put(split[0].trim(), split[1].trim());
        }

        setCookies(cookies);
    }

    @Override
    public void addCookies(Map<String, String> cookies) {
        this.cookies.putAll(cookies);
    }

    @Override
    public void setCookies(Map<String, String> cookies) {
        clearCookies();
        addCookies(cookies);
    }

    @Override
    public void clearCookies() {
        this.cookies.clear();
    }

    @Override
    @SneakyThrows
    public Response searchFromMusic(final Long ownerId, final String searchQuery, final String offset) {
        long start = currentTimeMillis();
        final Response response = proxiedConnection(PATH_BASE + "/al_audio.php", POST)
                .data("access_hash", "")
                .data("act", "load_section")
                .data("al", "1")
                .data("claim", "0")
                .data("offset", offset)
                .data("owner_id", ownerId.toString())
                .data("search_history", "0")
                .data("type", "search")
                .data("search_q", searchQuery)
                .execute();
        log.info(PERFORMANCE_MARKER, "REST Search from music {}ms", currentTimeMillis() - start);
        return response;
    }

    @Override
    @SneakyThrows
    public Response searchFromNewsFeed(String searchQuery) {
        long start = currentTimeMillis();
        final Response response = proxiedConnection(PATH_BASE + "/al_search.php", POST)
                .data("c[q]", searchQuery)
                .data("c[section]", "auto")
                .execute();
        log.info(PERFORMANCE_MARKER, "REST Search from news {}ms", currentTimeMillis() - start);
        return response;
    }

    @Override
    @SneakyThrows
    public Response reload(final String audioIds) {
        long start = currentTimeMillis();
        final Response response = proxiedConnection(PATH_BASE + "/al_audio.php", POST)
                .data("act", "reload_audio")
                .data("al", "1")
                .data("ids", audioIds)
                .execute();
        log.info(PERFORMANCE_MARKER, "REST reload {}ms", currentTimeMillis() - start);
        return response;
    }

    @Override
    @SneakyThrows
    public Response getContentLength(final String url) {
        return getOptionalWithRetries(this::getLength, url, 4, "get_content_length")
                .orElse(null);
    }

    private Optional<Response> getLength(final String url) {
        try {
            return ofNullable(proxiedConnection(url, HEAD)
                    .ignoreContentType(true)
                    .header("range", "bytes=0-1")
                    .execute());
        } catch (IOException e) {
            return empty();
        }
    }

    private Connection proxiedConnection(final String url, final Connection.Method method) {
        final Connection connection = connect(url).userAgent(USER_AGENT).cookies(cookies).method(method);

        return (configuration != null && configuration.getProxy() != null) ?
                connection.proxy(configuration.getProxy().getIp(), configuration.getProxy().getPort()) :
                connection;
    }
}
