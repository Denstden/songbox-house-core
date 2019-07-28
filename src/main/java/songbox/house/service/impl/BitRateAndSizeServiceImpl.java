package songbox.house.service.impl;

import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import songbox.house.domain.entity.VkAudio;
import songbox.house.service.BitRateAndSizeService;
import songbox.house.service.search.vk.VkFileSizeService;
import songbox.house.task.Downloader;
import songbox.house.task.SetBitRateTask;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

import static java.lang.System.currentTimeMillis;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.iterate;
import static lombok.AccessLevel.PRIVATE;
import static songbox.house.util.Constants.PERFORMANCE_MARKER;
import static songbox.house.util.ExecutorUtil.createExecutorService;

@Service
@Slf4j
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class BitRateAndSizeServiceImpl implements BitRateAndSizeService {

    VkFileSizeService vkFileSizeService;
    Downloader downloader;
    ExecutorService bitRatesExecutorService;

    public BitRateAndSizeServiceImpl(VkFileSizeService vkFileSizeService, Downloader downloader,
            @Value("${songbox.house.vk.bitrate.threads}") Integer bitRatesThreads) {
        this.vkFileSizeService = vkFileSizeService;
        this.downloader = downloader;
        this.bitRatesExecutorService = createExecutorService(bitRatesThreads);
    }

    @SneakyThrows
    @Override
    public List<VkAudio> calculateBitRatesAndSize(final List<VkAudio> audios) {
        final long startedMs = currentTimeMillis();
        CompletionService<Optional<VkAudio>> completionService = new ExecutorCompletionService<>(bitRatesExecutorService);
        final int size = audios.size();
        audios.forEach(audio -> completionService.submit(new SetBitRateTask(audio, vkFileSizeService, downloader)));
        final List<VkAudio> resultBitRates = getResultBitRates(completionService, size);
        log.info(PERFORMANCE_MARKER, "Bit rates for all {}ms", currentTimeMillis() - startedMs);
        return resultBitRates;
    }

    private List<VkAudio> getResultBitRates(CompletionService<Optional<VkAudio>> completionService, int size) {
        return iterate(0, i -> i++).limit(size)
                .map(i -> take(completionService))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private <T> Optional<T> take(CompletionService<Optional<T>> completionService) {
        try {
            return completionService.take().get();
        } catch (Exception e) {
            log.error("Take exception", e);
            return empty();
        }
    }
}
