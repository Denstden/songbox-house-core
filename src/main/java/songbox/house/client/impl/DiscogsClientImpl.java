package songbox.house.client.impl;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import songbox.house.client.DiscogsClient;
import songbox.house.service.TimeService;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Integer.valueOf;
import static java.lang.Thread.sleep;
import static java.text.MessageFormat.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.concurrent.ThreadLocalRandom.current;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.jsoup.Connection.Method.GET;
import static org.jsoup.Jsoup.connect;
import static songbox.house.util.RetryUtil.getOptionalWithRetries;

@Component
@Slf4j
@Data
public class DiscogsClientImpl implements DiscogsClient {
    private static final int RETRIES_TIMEOUT_MS = 1000;
    private static final long EXPIRED_RATE_LIMIT_UPDATED = -1;
    private static final AtomicLong RATE_LIMIT_UPDATED = new AtomicLong(EXPIRED_RATE_LIMIT_UPDATED);
    private static final AtomicInteger RATE_LIMIT_REMAINING = new AtomicInteger(100);

    private static final String RELEASES_SUB_PATH = "/releases";

    private static final String API_BASE = "https://api.discogs.com";
    private static final String WEBSITE_BASE = "https://www.discogs.com";
    private static final String WEBSITE_SEARCH_BASE = WEBSITE_BASE + "/search/";
    private static final String RELEASES = API_BASE + RELEASES_SUB_PATH + "/";
    private static final String MARKETPLACE_ITEM = API_BASE + "/marketplace/listings/";
    private static final String LABELS = API_BASE + "/labels/";
    private static final String USER_COLLECTION_RELEASES = API_BASE + "/users/{0}/collection/folders/0/releases?page={1}&per_page={2}&sort=added&sort_order=desc";
    private static final String USER_WANT_LIST = API_BASE + "/users/{0}/wants?page={1}&per_page={2}";
    private static final String ARTIST_RELEASES = API_BASE + "/artists/{0}/releases?page={1}&per_page={2}&sort=year&sort_order=desc";

    private static final String USER_AGENT = "Mozilla/5.0 (X11; U; Linux i586; en-US; rv:1.7.3) Gecko/20040924 Epiphany/1.4.4 (Ubuntu)";

    private final TimeService timeService;
    private final String consumerKey;
    private final String consumerSecret;
    private final Integer discogsRetries;

    public DiscogsClientImpl(final TimeService timeService,
            @Value("${discogs.app.consumer.key}") final String consumerKey,
            @Value("${discogs.app.consumer.secret}") final String consumerSecret,
            @Value("${discogs.client.retries}") final Integer discogsRetries) {
        this.timeService = timeService;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.discogsRetries = discogsRetries;
    }

    @Override
    public Response getRelease(final String releaseId) {
        final Connection connection = connect(RELEASES + releaseId)
                .userAgent(USER_AGENT)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .method(GET);

        return executeWithRetries(connection, "GetRelease");
    }

    @Override
    public Response getReleaseByLink(String discogsLink) {
        String url = discogsLink;
        if (!discogsLink.startsWith(WEBSITE_BASE)) {
            url = WEBSITE_BASE + discogsLink;
        }

        final Connection connection = connect(url)
                .userAgent(USER_AGENT)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .method(Connection.Method.GET);

        return executeWithRetries(connection, "GetReleaseByLink");
    }

    @Override
    public Response getLabelReleases(final String labelId, final Integer pageNumber, final Integer perPage) {
        final Connection connection = connect(LABELS + labelId + RELEASES_SUB_PATH + "?page=" + pageNumber + "&per_page=" + perPage)
                .userAgent(USER_AGENT)
                .ignoreContentType(true)
                .method(GET);

        return executeWithRetries(connection, "GetLabelReleases");
    }

    @Override
    public Response getMarketplaceItem(final String itemId) {
        final Connection connection = connect(MARKETPLACE_ITEM + itemId)
                .userAgent(USER_AGENT)
                .ignoreContentType(true)
                .method(GET);

        return executeWithRetries(connection, "GetMarketplaceItem");
    }

    @Override
    public Response getUserCollectionItems(final String userName, final Integer pageNumber, final Integer perPage) {
        final Connection connection = connect(format(USER_COLLECTION_RELEASES, userName, pageNumber, perPage))
                .userAgent(USER_AGENT)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .method(GET);

        return executeWithRetries(connection, "GetUserCollectionItems");
    }

    @Override
    public Response getArtistReleases(String artistId, Integer pageNumber, Integer perPage) {
        final Connection connection = connect(format(ARTIST_RELEASES, artistId, pageNumber, perPage))
                .userAgent(USER_AGENT)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .method(GET);

        return executeWithRetries(connection, "GetArtistReleases");
    }

    @Override
    @SneakyThrows
    public Response searchArtwork(final String searchQuery) {
        return search(searchQuery);
    }

    @Override
    public Response search(String searchQuery) {
        final Connection connection = connect(WEBSITE_SEARCH_BASE)
                .data("type", "all")
                .data("q", searchQuery)
                .ignoreContentType(true)
                .method(Connection.Method.GET);

        return executeWithRetries(connection, "Search", "2");
    }

    @Override
    public Response getUserWantListItems(String userName, int pageNumber, Integer releasesPageSize) {
        final Connection connection = connect(format(USER_WANT_LIST, userName, pageNumber, releasesPageSize))
                .userAgent(USER_AGENT)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .method(GET);

        return executeWithRetries(connection, "GetUserWantListItems");
    }

    @SneakyThrows
    private Response executeWithRetries(Connection connection, String... params) {
        final int maxRetries = params.length >= 2 ? valueOf(params[1]) : discogsRetries;

        return getOptionalWithRetries(this::execute, connection, maxRetries, "execute_discogs")
                .orElse(null);
    }

    private Optional<Response> execute(Connection connection) {
        try {
            checkRateLimit();

            connection.header("Authorization", getAppAuthParams());

            Response response = connection.execute();

            final int statusCode = response.statusCode();

            updateRateLimit(response);

            if (statusCode != 200 && statusCode != 404) {
                sleep(RETRIES_TIMEOUT_MS / 10 * current().nextInt(1, 10));
                return empty();
            } else {
                return of(response);
            }
        } catch (InterruptedException | IOException e) {
            return empty();
        }
    }

    private void checkRateLimit() throws InterruptedException {
        final long updatedAtSecond = RATE_LIMIT_UPDATED.get();
        if (EXPIRED_RATE_LIMIT_UPDATED != updatedAtSecond) {
            final long difference = timeService.getNowSeconds() - updatedAtSecond;
            if (difference > MINUTES.toSeconds(1)) {
                RATE_LIMIT_UPDATED.set(EXPIRED_RATE_LIMIT_UPDATED);
            } else {
                int remaining = RATE_LIMIT_REMAINING.get();
                if (remaining == 0) {
                    long sleepTime = (60 - difference) * 1000;
                    log.debug("Sleeping for {} (discogs rate limiter)", sleepTime);
                    sleep(sleepTime);
                }
            }
        }
    }

    private void updateRateLimit(Response response) {
        String remainingCalls = response.header("X-Discogs-Ratelimit-Remaining");
        if (isNotBlank(remainingCalls)) {
            RATE_LIMIT_REMAINING.set(valueOf(remainingCalls));
            RATE_LIMIT_UPDATED.set(timeService.getNowSeconds());
        }
    }

    private String getAppAuthParams() {
        return format("Discogs key={0}, secret={1}", consumerKey, consumerSecret);
    }
}
