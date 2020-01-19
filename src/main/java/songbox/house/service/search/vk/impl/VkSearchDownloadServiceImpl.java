package songbox.house.service.search.vk.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import songbox.house.domain.entity.SearchHistory;
import songbox.house.domain.entity.Track;
import songbox.house.service.search.vk.VkSearchDownloadService;

import java.util.Optional;
import java.util.Set;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
@AllArgsConstructor
public class VkSearchDownloadServiceImpl implements VkSearchDownloadService {

    @Override
    public Optional<Track> searchAndDownload(String authors, String title, Set<String> genres,
            Long collectionId, SearchHistory searchHistory) {
        return Optional.empty();
    }

    @Override
    public void searchAndDownloadAsync(String authors, String title, Set<String> genres,
            Long collectionId, SearchHistory searchHistory, boolean only320) {

    }
}
