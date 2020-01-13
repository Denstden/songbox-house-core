package songbox.house.task;

import com.iheartradio.m3u8.PlaylistParser;
import com.iheartradio.m3u8.data.Playlist;
import com.iheartradio.m3u8.data.TrackData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import songbox.house.util.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Optional;

import static com.iheartradio.m3u8.Encoding.UTF_8;
import static com.iheartradio.m3u8.Format.EXT_M3U;
import static java.net.Proxy.Type.HTTP;
import static java.util.Collections.emptyList;
import static songbox.house.util.DownloadUtil.downloadBytes;


@Slf4j
@Data
@Component
public class Downloader {

    private final Configuration configuration;

    public Optional<byte[]> downloadBytesVK(String url) {
        Proxy vkProxy = getProxy();
        return downloadBytes(url, vkProxy);
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

    private Proxy getProxy() {
        return configuration == null || configuration.getProxy() == null
                ? null
                : new Proxy(HTTP, new InetSocketAddress(configuration.getProxy().getIp(), configuration.getProxy().getPort()));
    }
}
