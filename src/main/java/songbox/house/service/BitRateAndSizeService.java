package songbox.house.service;

import songbox.house.domain.entity.VkAudio;

import java.util.List;

public interface BitRateAndSizeService {
    List<VkAudio> calculateBitRatesAndSize(final List<VkAudio> list);
}
