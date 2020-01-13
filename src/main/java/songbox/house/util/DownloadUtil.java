package songbox.house.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.io.IOUtils.toByteArray;
import static songbox.house.util.RetryUtil.DEFAULT_RETRIES;
import static songbox.house.util.RetryUtil.getOptionalWithRetries;

@Slf4j
public class DownloadUtil {
    private static final int DEFAULT_CONNECT_TIMEOUT = 10_000;
    private static final int DEFAULT_READ_TIMEOUT = 10_000;

    public static Optional<byte[]> downloadBytes(String url) {
        return download(url, null);
    }

    public static Optional<byte[]> downloadBytes(String url, Proxy proxy) {
        return download(url, proxy);
    }

    private static Optional<byte[]> download(String urlStr, Proxy proxy) {
        return getOptionalWithRetries(DownloadUtil::doDownload, urlStr, proxy, DEFAULT_RETRIES, "download_bytes");
    }

    private static Optional<byte[]> doDownload(String urlStr, Proxy proxy) {
        try {
            final URL url = new URL(urlStr);
            final HttpURLConnection connection = getConnection(url, proxy);
            final InputStream inputStream = connection.getInputStream();
            final byte[] bytes = toByteArray(inputStream);
            return of(bytes);
        } catch (Exception e) {
            log.debug("Retryable exception", e);
            return empty();
        }
    }

    private static HttpURLConnection getConnection(URL url, Proxy proxy) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) (nonNull(proxy) ? url.openConnection(proxy) : url.openConnection());
        connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
        connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
        return connection;
    }
}
