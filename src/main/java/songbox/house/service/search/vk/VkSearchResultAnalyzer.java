package songbox.house.service.search.vk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import songbox.house.domain.dto.response.vk.VkSearchResponseDto;
import songbox.house.domain.entity.VkAudio;
import songbox.house.util.ArtistTitleUtil;
import songbox.house.util.Pair;
import songbox.house.util.compare.ArtistTitleComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static com.google.api.client.util.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static songbox.house.util.ArtistTitleUtil.extractArtistTitle;

@Component
@Slf4j
public class VkSearchResultAnalyzer {
    private static final Integer MIN_DURATION_SECONDS = 120; // 2 min
    private static final Double MAX_SIZE_MB = 50.;
    private static final int ARTIST_TITLE_FILTER_THRESHOLD = 40; // 40%

    private ArtistTitleComparator artistTitleComparator = new ArtistTitleComparator();

    public void filterByArtistTitle(String searchQuery, VkSearchResponseDto vkSearchResponseDto) {
        Pair<String, String> artistTitle = extractArtistTitle(searchQuery);
        List<ArrayList> filteredList = vkSearchResponseDto
                .getList()
                .stream()
                .filter(it -> {
                    if (!(it instanceof List)) {
                        return false;
                    }

                    Pair<String, String> comparableArtistTitle = ArtistTitleUtil.extractArtistTitleFromVkSearchResultDtoList((List) it);
                    int compareResult = artistTitleComparator.compareArtistTitle(artistTitle, comparableArtistTitle);
                    return compareResult >= ARTIST_TITLE_FILTER_THRESHOLD;
                }).collect(toList());
        vkSearchResponseDto.setList(filteredList);
    }

    public List<VkAudio> filterIncorrectResults(final Collection<VkAudio> audios, final String query) {
        final Pair<String, String> artistsTitle = extractArtistTitle(query);
        return filterIncorrectResults(audios, artistsTitle.getLeft(), artistsTitle.getRight(), false);
    }

    public List<VkAudio> filterIncorrectResults(final Collection<VkAudio> audios, final String authors, final String title,
            final boolean only320) {
        List<VkAudio> all = newArrayList(audios);
        if (only320) {
            all = audios.stream()
                    .filter(vkAudio -> vkAudio != null && vkAudio.getBitRate() != null && vkAudio.getBitRate() == 320)
                    .collect(toList());
        }

        log.trace("Checking conformation authors: \"{}\", title: \"{}\"", authors, title);
        List<VkAudio> result = checkTitleAndArtistConforms(all, authors, title);
        result = checkArtistConforms(result, authors);
        result = checkTitleConforms(result, title);
        result = checkDurationMoreThan(result);
        return checkSizeLessThan(result);
    }

    private List<VkAudio> checkDurationMoreThan(final List<VkAudio> audios) {
        final Predicate<VkAudio> predicate = vkAudio -> (vkAudio.getDuration() > MIN_DURATION_SECONDS);

        return filterByPredicateIfExists(audios, predicate);
    }

    private List<VkAudio> checkSizeLessThan(final List<VkAudio> audios) {
        final Predicate<VkAudio> predicate = vkAudio -> (vkAudio.getSizeMb() != null && vkAudio.getSizeMb() < MAX_SIZE_MB);

        return filterByPredicateIfExists(audios, predicate);
    }

    private List<VkAudio> checkTitleAndArtistConforms(final List<VkAudio> audios, final String authors,
            final String title) {
        final Predicate<VkAudio> predicate = vkAudio -> (authors.equalsIgnoreCase(vkAudio.getArtist()) && title.equalsIgnoreCase(vkAudio.getTitle()));

        return filterByPredicateIfExists(audios, predicate);
    }

    private List<VkAudio> checkArtistConforms(final List<VkAudio> audios, final String authors) {
        final Predicate<VkAudio> artistConforms = vkAudio -> (authors.equalsIgnoreCase(vkAudio.getArtist()) ||
                vkAudio.getArtist().toLowerCase().contains(authors.toLowerCase()));

        return filterByPredicateIfExists(audios, artistConforms);
    }

    private List<VkAudio> checkTitleConforms(final List<VkAudio> audios, final String title) {
        final Predicate<VkAudio> titleConforms = vkAudio -> (title.equalsIgnoreCase(vkAudio.getTitle()) ||
                vkAudio.getTitle().toLowerCase().contains(title.toLowerCase()));

        return filterByPredicateIfExists(audios, titleConforms);
    }

    private List<VkAudio> filterByPredicateIfExists(final List<VkAudio> audios, final Predicate<VkAudio> predicate) {
        final boolean hasConformation = audios.stream().anyMatch(predicate);

        if (hasConformation) {
            return audios.stream().filter(predicate).collect(toList());
        } else {
            return audios;
        }
    }
}
