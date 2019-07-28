package songbox.house.service.search.vk.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songbox.house.domain.entity.VkAudio;
import songbox.house.domain.event.vk.VkDownloadStatusEvent;
import songbox.house.exception.NotExistsException;
import songbox.house.repository.VkAudioRepository;
import songbox.house.service.search.vk.VkAudioService;

@Transactional
@Service
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class VkAudioServiceImpl implements VkAudioService {

    VkAudioRepository vkAudioRepository;

    @Override
    public void save(final VkAudio audio) {
        vkAudioRepository.save(audio);
    }

    @Override
    public void onDownloadStatusEvent(final VkDownloadStatusEvent event) {
        final VkAudio audio = event.getAudio();
        if (audio != null) {
            final VkAudio entity = vkAudioRepository.findById(audio.getId())
                    .orElseThrow(() -> new NotExistsException("Audio not exist"));
            entity.setStatus(event.getStatus());
            save(entity);
        } else {
            log.error("Can't download vk audio {}", event);
        }
    }

}
