package songbox.house.task;

import com.iheartradio.m3u8.PlaylistParser;
import com.iheartradio.m3u8.data.Playlist;
import com.iheartradio.m3u8.data.TrackData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static com.iheartradio.m3u8.Encoding.UTF_8;
import static com.iheartradio.m3u8.Format.EXT_M3U;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.io.IOUtils.toByteArray;
import static songbox.house.util.RetryUtil.DEFAULT_RETRIES;
import static songbox.house.util.RetryUtil.getOptionalWithRetries;


@Slf4j
@Data
@Component
public class Downloader {

    private final HTTPConnectionProvider connectionProvider;

    public Optional<byte[]> downloadBytesVK(String url) {
        return downloadBytes(url, true);
    }

    public List<TrackData> getPartsMetadata(String indexUrl) {
        byte[] indexM3U8 = new byte[0];
        try {
            indexM3U8 = downloadBytesVK(indexUrl)
                    .orElseThrow(() -> new RuntimeException("Can't download index m3u8 file"));
            InputStream stream = new ByteArrayInputStream(indexM3U8);
            PlaylistParser playlistParser = new PlaylistParser(stream, EXT_M3U, UTF_8);
            Playlist parse = playlistParser.parse();
            return parse.getMediaPlaylist().getTracks();
        } catch (Exception e) {
            log.warn("Can't parse index file {}, size {}bytes", indexUrl, indexM3U8.length);
            return emptyList();
        }
    }


    public Optional<byte[]> downloadBytes(String url) {
        return downloadBytes(url, false);
    }

    private Optional<byte[]> downloadBytes(String urlStr, boolean isProxied) {
        return getOptionalWithRetries(this::download, urlStr, isProxied, DEFAULT_RETRIES, "download_bytes");
    }

    private Optional<byte[]> download(String urlStr, boolean isProxied) {
        try {
            final URL url = new URL(urlStr);
            final HttpURLConnection connection = connectionProvider.getConnection(url, isProxied);
            final InputStream inputStream = connection.getInputStream();
            final byte[] bytes = toByteArray(inputStream);
            return of(bytes);
        } catch (Exception e) {
            log.debug("Retryable exception", e);
            return empty();
        }
    }
}
