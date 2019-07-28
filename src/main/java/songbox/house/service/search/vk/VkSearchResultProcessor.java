package songbox.house.service.search.vk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import songbox.house.domain.dto.request.vk.VkSearchAudioRequestDto;
import songbox.house.domain.dto.response.vk.VkSearchResponseDto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.text.MessageFormat.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static songbox.house.util.parser.HexParser.parseHex;

@Component
@Slf4j
public class VkSearchResultProcessor {

    private static final String CONTENT_IDS_FORMAT = "{0}_{1}_{2}";

    public VkSearchAudioRequestDto processSearchResults(final List<VkSearchResponseDto> searchResult) {
        final Iterator<String> contentIdsFromNews = searchResult.stream()
                .filter(VkSearchResponseDto::getFromNews)
                .findFirst()
                .map(this::getContentIds)
                .orElse(emptyList())
                .iterator();

        final Iterator<String> contentIdsFromMusic = searchResult.stream()
                .filter(dto -> !dto.getFromNews())
                .map(this::getContentIds)
                .flatMap(List::stream)
                .iterator();

        return getMergedContentIds(contentIdsFromNews, contentIdsFromMusic);
    }

    private VkSearchAudioRequestDto getMergedContentIds(Iterator<String> contentIdsFromNews,
            Iterator<String> contentIdsFromMusic) {
        final List<String> mergedContentIds = newArrayList();
        do {
            safeAdd(contentIdsFromNews, mergedContentIds);
            safeAdd(contentIdsFromMusic, mergedContentIds);
        } while (contentIdsFromNews.hasNext() || contentIdsFromMusic.hasNext());

        return new VkSearchAudioRequestDto(mergedContentIds);
    }

    private void safeAdd(Iterator<String> iterator, List<String> result) {
        if (iterator.hasNext()) {
            result.add(iterator.next());
        }
    }

    private List<String> getContentIds(VkSearchResponseDto vkSearchResponseDto) {
        final List<ArrayList> trackInfos = vkSearchResponseDto.getList();

        if (trackInfos == null) return emptyList();

        return trackInfos.stream()
                .map(this::getContentIds)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

    }

    private Optional<String> getContentIds(final ArrayList trackFields) {
        return ofNullable(trackFields).flatMap(this::processTrackFields);
    }

    private Optional<String> processTrackFields(final ArrayList trackFields) {
        final LinkedHashMap props = (LinkedHashMap) trackFields.get(15);

        final String hex = (String) trackFields.get(13);

        return ofNullable(props)
                .map(p -> p.get("content_id"))
                .map(String::valueOf)
                .map(contentId -> getContentIdsWithHex(contentId, hex));
    }

    private String getContentIdsWithHex(String contentId, String hex) {
        return parseHex(hex)
                .map(pair -> format(CONTENT_IDS_FORMAT, contentId, pair.getLeft(), pair.getRight()))
                .orElse(contentId);
    }
}
