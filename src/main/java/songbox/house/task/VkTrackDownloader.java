package songbox.house.task;

import com.iheartradio.m3u8.PlaylistParser;
import com.iheartradio.m3u8.data.EncryptionData;
import com.iheartradio.m3u8.data.Playlist;
import com.iheartradio.m3u8.data.TrackData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import songbox.house.domain.entity.VkAudio;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static com.iheartradio.m3u8.Encoding.UTF_8;
import static com.iheartradio.m3u8.Format.EXT_M3U;
import static com.iheartradio.m3u8.data.EncryptionMethod.AES;
import static java.lang.System.arraycopy;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.copyOf;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.getInstance;
import static songbox.house.util.Constants.PERFORMANCE_MARKER;
import static songbox.house.util.ThreadLocalAuth.applyContext;
import static songbox.house.util.VkUtil.getPartUrl;

@Slf4j
@Component
public class VkTrackDownloader {

    private final Downloader downloader;
    private final Cipher cipher;
    private final ExecutorService executorService;

    @Autowired
    public VkTrackDownloader(Downloader downloader,
            @Value("${songbox.house.vk.download.threads}") Integer countThreads)
            throws NoSuchPaddingException, NoSuchAlgorithmException {
        this.downloader = downloader;
        this.cipher = getInstance("AES/CBC/PKCS5Padding");
        this.executorService = newFixedThreadPool(countThreads);
    }

    public Optional<byte[]> downloadM3U8Content(VkAudio vkAudio) {
        String indexUrl = vkAudio.getUrl();
        final long startMs = currentTimeMillis();
        final List<TrackData> parts = getPartsMetadata(indexUrl);

        final byte[] aes128Key = getAES128Key(parts).orElse(null);

        final int countParts = parts.size();
        final CompletionService<Optional<OrderedPartDto>> completionService = new ExecutorCompletionService<>(executorService);

        submitPartsDownloading(parts, indexUrl, aes128Key != null, countParts, completionService);

        final Map<Integer, byte[]> partsContent = getPartsContent(aes128Key, countParts, completionService);

        if (countParts != partsContent.size()) {
            log.warn("Can't download all parts of the file");
            return empty();
        } else {
            final byte[] bytes = partsContent.keySet().stream()
                    .sorted()
                    .map(partsContent::get)
                    .reduce(new byte[] {}, this::concat);
            log.info(PERFORMANCE_MARKER, "DownloadedAndMerged m3u8 {}ms", currentTimeMillis() - startMs);
            return of(bytes);
        }
    }

    public Optional<byte[]> downloadMp3Content(VkAudio audio) {
        final long startMs = currentTimeMillis();
        final Optional<byte[]> mp3Bytes = downloader.downloadBytesVK(audio.getUrl());
        log.info(PERFORMANCE_MARKER, "Downloaded mp3 {}ms", currentTimeMillis() - startMs);
        return mp3Bytes;
    }

    public List<TrackData> getPartsMetadata(String indexUrl) {
        try {
            byte[] indexM3U8 = downloader.downloadBytesVK(indexUrl)
                    .orElseThrow(() -> new RuntimeException("Can't download index m3u8 file"));
            InputStream stream = new ByteArrayInputStream(indexM3U8);
            PlaylistParser playlistParser = new PlaylistParser(stream, EXT_M3U, UTF_8);
            Playlist parse = playlistParser.parse();
            return parse.getMediaPlaylist().getTracks();
        } catch (Exception e) {
            log.warn("Can't parse index file {}", indexUrl);
            return emptyList();
        }
    }

    private Optional<byte[]> getAES128Key(List<TrackData> parts) {
        return parts.parallelStream()
                .filter(part -> needDecrypt(part.getEncryptionData()))
                .map(TrackData::getEncryptionData)
                .map(EncryptionData::getUri)
                .map(downloader::downloadBytesVK)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private void submitPartsDownloading(List<TrackData> parts, String indexUrl, boolean existsAESKey, int countParts,
            CompletionService<Optional<OrderedPartDto>> completionService) {

        for (AtomicInteger i = new AtomicInteger(0); i.get() < countParts; i.incrementAndGet()) {
            final int index = i.get();
            final TrackData trackData = parts.get(index);
            final String url = getPartUrl(indexUrl, trackData.getUri());
            final boolean needDecrypt = existsAESKey && needDecrypt(trackData.getEncryptionData());

            completionService.submit(applyContext(() -> downloadPart(url, index, needDecrypt)));
        }
    }

    private Map<Integer, byte[]> getPartsContent(byte[] aes128Key, int countParts,
            CompletionService<Optional<OrderedPartDto>> completionService) {

        final Map<Integer, byte[]> orderedParts = new HashMap<>();
        for (int i = 0; i < countParts; i++) {
            try {
                completionService.take().get()
                        .ifPresent(dto -> orderedParts.put(
                                dto.getOrder(),
                                decryptIfNeed(dto.getContent(), dto.isNeedDecrypt(), aes128Key)
                                )
                        );
            } catch (Exception e) {
                log.warn("Can't download part {}", i, e);
            }
        }
        return orderedParts;
    }

    private boolean needDecrypt(EncryptionData encryptionData) {
        return encryptionData != null && AES == encryptionData.getMethod();
    }

    private byte[] decryptIfNeed(byte[] content, boolean needDecrypt, byte[] aes128Key) {
        if (!needDecrypt || aes128Key == null) return content;
        return decrypt(content, aes128Key);
    }

    private byte[] decrypt(byte[] data, byte[] key) {
        try {
            byte[] iv = new byte[cipher.getBlockSize()];
            AlgorithmParameterSpec spec = new IvParameterSpec(iv);
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(DECRYPT_MODE, secretKey, spec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            // lets try with not decrypted
            return data;
        }
    }

    private Optional<OrderedPartDto> downloadPart(String url, int order, boolean needDecrypt) {
        return downloader.downloadBytesVK(url)
                .map(bytes -> new OrderedPartDto(order, bytes, needDecrypt));
    }

    private byte[] concat(byte[] a, byte[] b) {
        int lenA = a.length;
        int lenB = b.length;
        byte[] c = copyOf(a, lenA + lenB);
        arraycopy(b, 0, c, lenA, lenB);
        return c;
    }

    @Data
    private class OrderedPartDto {
        private final int order;
        private final byte[] content;
        private final boolean needDecrypt;
    }
}
