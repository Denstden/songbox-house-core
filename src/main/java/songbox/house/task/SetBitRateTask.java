package songbox.house.task;

import com.iheartradio.m3u8.data.TrackData;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import songbox.house.domain.entity.VkAudio;
import songbox.house.service.search.vk.VkFileSizeService;
import songbox.house.util.ThreadChange;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Float.compare;
import static java.lang.System.currentTimeMillis;
import static java.util.Optional.of;
import static lombok.AccessLevel.PRIVATE;
import static songbox.house.util.BitRateCalculator.calculateBitRate;
import static songbox.house.util.Constants.PERFORMANCE_MARKER;
import static songbox.house.util.VkUtil.getPartUrl;

@Data
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Slf4j
public class SetBitRateTask extends ThreadChange.LocalAuthCallable<Optional<VkAudio>> {

    VkAudio vkAudio;
    VkFileSizeService vkFileSizeService;
    Downloader downloader;

    public SetBitRateTask(VkAudio vkAudio, VkFileSizeService vkFileSizeService,
            Downloader downloader) {
        this.vkAudio = vkAudio;
        this.vkFileSizeService = vkFileSizeService;
        this.downloader = downloader;
    }

    @Override
    public Optional<VkAudio> callWithContext() {
        final long startedMs = currentTimeMillis();
        final String indexUrl = vkAudio.getUrl();
        final Short bitrate = vkAudio.isM3U8() ? getBitrateForM3U8(indexUrl) : getBitrateForMP3(indexUrl);
        log.info(PERFORMANCE_MARKER, "Bit rate {} for 1 track: {}ms", bitrate, currentTimeMillis() - startedMs);
        vkAudio.setBitRate(bitrate);
        return of(vkAudio);
    }

    private Short getBitrateForMP3(String url) {
        log.info("Calculate bitrate for MP3");
        long sizeBytes = vkFileSizeService.getSizeBytes(url);
        return calculateBitRate(vkAudio.getDuration(), sizeBytes);
    }

    private Short getBitrateForM3U8(String url) {
        log.info("Calculate bitrate for M3U8");

        final List<TrackData> partsMetadata = downloader.getPartsMetadata(url);
        final TrackData partWithMaxDuration = newArrayList(partsMetadata).stream()
                .max((o1, o2) -> compare(o1.getTrackInfo().duration, o2.getTrackInfo().duration))
                .orElseThrow(() -> new RuntimeException("File should contain at least 1 part"));

        final int duration = new Float(partWithMaxDuration.getTrackInfo().duration).intValue();

        final String partUrl = getPartUrl(url, partWithMaxDuration.getUri());
        final Long sizeLong = vkFileSizeService.getSizeBytes(partUrl);

        return calculateBitRate(duration, sizeLong);
    }
}
