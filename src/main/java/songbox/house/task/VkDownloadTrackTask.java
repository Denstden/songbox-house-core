package songbox.house.task;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import lombok.extern.slf4j.Slf4j;
import songbox.house.domain.entity.VkAudio;
import songbox.house.util.ThreadChange;
import ws.schild.jave.AudioAttributes;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncodingAttributes;
import ws.schild.jave.MultimediaObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.lang.System.currentTimeMillis;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.apache.commons.io.IOUtils.toByteArray;
import static songbox.house.util.Constants.PERFORMANCE_MARKER;
import static songbox.house.util.RetryUtil.getOptionalWithRetries;

@Slf4j
public class VkDownloadTrackTask implements Callable<Optional<byte[]>> {

    private static final String AUDIO_CODEC = "libmp3lame";

    private final VkAudio vkAudio;
    private final CompletableFuture<byte[]> artwork;
    private final VkTrackDownloader vkTrackDownloader;
    private final Encoder encoder;
    private final Boolean mp3EncoderNewSwitcher;
    private final Boolean mp3EncoderLowBitrate;
    private final Integer downloadRetries;
    private final Integer encodeRetries;

    public VkDownloadTrackTask(VkAudio vkAudio, CompletableFuture<byte[]> artwork, VkTrackDownloader vkTrackDownloader,
            Encoder encoder, Boolean mp3EncoderNewSwitcher, Boolean mp3EncoderLowBitrate, Integer downloadRetries,
            Integer encodeRetries) {
        this.vkAudio = vkAudio;
        this.artwork = artwork;
        this.vkTrackDownloader = vkTrackDownloader;
        this.encoder = encoder;
        this.mp3EncoderNewSwitcher = mp3EncoderNewSwitcher;
        this.mp3EncoderLowBitrate = mp3EncoderLowBitrate;
        this.downloadRetries = downloadRetries;
        this.encodeRetries = encodeRetries;
    }

    @Override
    public Optional<byte[]> call() {
        final String indexUrl = vkAudio.getUrl();
        log.info("Starting downloading {} - {}, index url {}", vkAudio.getArtist(), vkAudio.getTitle(), indexUrl);
        return getOptionalWithRetries(vkAudio.isM3U8() ? vkTrackDownloader::downloadM3U8Content : vkTrackDownloader::downloadMp3Content,
                vkAudio, downloadRetries, "download")
                .flatMap(downloaded -> getOptionalWithRetries(this::encodeAndSetTags, downloaded, encodeRetries, "encode"));
    }

    //TODO re-implement encoding without files, we create 3 files for each track - it's very slow
    private Optional<byte[]> encodeAndSetTags(byte[] bytes) {
        File unencodedFile = null;
        File encodedFile = null;
        File fileWithTags = null;

        try {
            unencodedFile = createTempFile();
            writeBytes(bytes, unencodedFile);

            // Encode
            if (vkAudio.isM3U8()) {
                final long startMs = currentTimeMillis();
                encodedFile = createTempFile();
                final EncodingAttributes attrs = createAttributes();
                (mp3EncoderNewSwitcher ? new Encoder() : encoder).encode(new MultimediaObject(unencodedFile), encodedFile, attrs);
                final long encodedMs = currentTimeMillis();
                log.info(PERFORMANCE_MARKER, "Encoded {}ms", encodedMs - startMs);
            }

            // Set ID3 Tags
            final Mp3File mp3File = createMp3File(vkAudio.isM3U8() ? encodedFile : unencodedFile);
            fileWithTags = createTempFile();
            mp3File.save(fileWithTags.getAbsolutePath());

            try (final FileInputStream fileInputStream = new FileInputStream(fileWithTags)) {
                final byte[] byteArray = toByteArray(fileInputStream);
                vkAudio.setSizeMb(byteArray.length / 1024 / 1024.);
                return of(byteArray);
            }
        } catch (Exception e) {
            log.warn("Can't encode or set tags for mp3 file {}", vkAudio.getFilename(), e);
            return empty();
        } finally {
            if (unencodedFile != null) {
                unencodedFile.delete();
            }
            if (encodedFile != null) {
                encodedFile.delete();
            }
            if (fileWithTags != null) {
                fileWithTags.delete();
            }
        }
    }

    private EncodingAttributes createAttributes() {
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec(AUDIO_CODEC);
        audio.setBitRate(1000 * (mp3EncoderLowBitrate ? 128 : ofNullable(vkAudio.getBitRate()).orElse((short) 320)));
        audio.setChannels(2);
        audio.setSamplingRate(48000);

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("mp3");
        attrs.setAudioAttributes(audio);
        return attrs;
    }

    private Mp3File createMp3File(final File tempFile) throws Exception {
        final Mp3File mp3File = new Mp3File(tempFile);
        final ID3v2 id3v2Tag = initId3v2Tag(mp3File);
        mp3File.setId3v2Tag(id3v2Tag);
        return mp3File;
    }

    private ID3v2 initId3v2Tag(final Mp3File mp3File) throws ExecutionException, InterruptedException {
        // Set artwork, title, etc...
        ID3v2 id3v2Tag = createID3v2Tag(mp3File);

        id3v2Tag.setTitle(vkAudio.getTitle());
        id3v2Tag.setTrack(vkAudio.getTitle());
        id3v2Tag.setArtist(vkAudio.getArtist());

        if (artwork != null) {
            id3v2Tag.setAlbumImage(artwork.get(), "image/jpg");
        }
        return id3v2Tag;
    }

    private ID3v2 createID3v2Tag(Mp3File mp3File) {
        ID3v2 id3v2Tag = new ID3v24Tag();
        mp3File.setId3v2Tag(id3v2Tag);
        return id3v2Tag;
    }

    private void writeBytes(final byte[] bytes, final File file) throws IOException {
        try (final FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(bytes);
        }
    }

    private File createTempFile() throws IOException {
        return File.createTempFile("" + new Random().nextInt(), ".mp3");
    }
}
