package songbox.house.service.search.vk.impl;

import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songbox.house.converter.VkAudioToTrackConverter;
import songbox.house.domain.TrackSource;
import songbox.house.domain.entity.Track;
import songbox.house.domain.entity.TrackContent;
import songbox.house.domain.entity.VkAudio;
import songbox.house.domain.event.vk.VkDownloadFailEvent;
import songbox.house.domain.event.vk.VkDownloadFinishEvent;
import songbox.house.domain.event.vk.VkDownloadSuccessEvent;
import songbox.house.exception.DownloadException;
import songbox.house.service.search.vk.VkDownloadService;
import songbox.house.task.Downloader;
import songbox.house.task.VkDownloadTrackTask;
import songbox.house.task.VkTrackDownloader;
import ws.schild.jave.Encoder;

import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

import static java.util.Optional.empty;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static lombok.AccessLevel.PRIVATE;
import static songbox.house.util.ExecutorUtil.createExecutorService;

@Slf4j
@Service
@Transactional
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class VkDownloadServiceImpl implements VkDownloadService {
    private static final String MP3_EXTENSION = "mp3";

    ExecutorService executorService;
    ApplicationEventPublisher publisher;
    VkAudioToTrackConverter converter;
    Downloader downloader;
    VkTrackDownloader vkTrackDownloader;
    Encoder encoder;
    Boolean mp3EncoderNewSwitcher;
    Boolean mp3EncoderLowBitrate;
    Integer downloadRetries;
    Integer encodeRetries;

    @Autowired
    public VkDownloadServiceImpl(ApplicationEventPublisher publisher,
            VkAudioToTrackConverter converter, Downloader downloader,
            VkTrackDownloader vkTrackDownloader, Encoder encoder,
            @Value("${songbox.house.vk.encoder.new_instance}") Boolean mp3EncoderNewSwitcher,
            @Value("${songbox.house.vk.encoder.low_bitrate}") Boolean mp3EncoderLowBitrate,
            @Value("${songbox.house.vk.encoder.retries}") Integer encodeRetries,
            @Value("${songbox.house.vk.download.retries}") Integer downloadRetries,
            @Value("${songbox.house.vk.download.threads_per_one}") Integer downloadThreads) {
        this.publisher = publisher;
        this.converter = converter;
        this.downloader = downloader;
        this.vkTrackDownloader = vkTrackDownloader;
        this.encoder = encoder;
        this.mp3EncoderNewSwitcher = mp3EncoderNewSwitcher;
        this.mp3EncoderLowBitrate = mp3EncoderLowBitrate;
        this.downloadRetries = downloadRetries;
        this.encodeRetries = encodeRetries;
        this.executorService = createExecutorService(downloadThreads);
    }

    @Async
    @Override
    public void downloadAsync(final VkAudio audio, final Set<String> genres, final Long collectionId) {
        download(audio, genres, collectionId);
    }

    @SneakyThrows
    @Override
    public Optional<Track> download(final VkAudio audio, final Set<String> genres, final Long collectionId) {
        final CompletionService<Optional<byte[]>> completionService = new ExecutorCompletionService<>(executorService);

        final CompletableFuture<byte[]> artwork = supplyAsync(() -> getArtwork(audio.getArtworkSrc()));

        completionService.submit(new VkDownloadTrackTask(audio, artwork, vkTrackDownloader, encoder,
                mp3EncoderNewSwitcher, mp3EncoderLowBitrate, downloadRetries, encodeRetries));

        final Optional<Track> track = downloadTrack(completionService, audio, genres, collectionId);

        publisher.publishEvent(new VkDownloadFinishEvent(this, audio));

        return track;
    }

    private byte[] getArtwork(String url) {
        return downloader.downloadBytes(url).orElse(null);
    }

    private Optional<Track> downloadTrack(final CompletionService<Optional<byte[]>> completionService,
            final VkAudio audio,
            final Set<String> genres, final Long collectionId) throws InterruptedException {
        try {
            return completionService.take().get()
                    .map(content -> createTrack(audio, content, genres, collectionId));
        } catch (final ExecutionException ex) {
            if (ex.getCause() instanceof SocketTimeoutException) {
                log.error("{}", ex);
                publisher.publishEvent(new VkDownloadFailEvent(this, new DownloadException(ex.getCause(), null)));
            }
            publisher.publishEvent(new VkDownloadFailEvent(this, (DownloadException) ex.getCause()));
            return empty();
        }
    }

    private Track createTrack(final VkAudio audio, final byte[] content, final Set<String> genres,
            final Long collectionId) {
        final Track track = converter.convert(audio);
        final TrackContent trackContent = new TrackContent();
        trackContent.setContent(content);
        track.setContent(trackContent);
        track.setTrackSource(TrackSource.VK);
        track.setExtension(MP3_EXTENSION);
        publisher.publishEvent(new VkDownloadSuccessEvent(this, audio, track, genres, collectionId));
        return track;
    }
}
