package songbox.house.util.parser;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import songbox.house.domain.dto.response.youtube.YoutubeSongDto;
import songbox.house.util.ArtistsTitle;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.valueOf;
import static java.lang.Math.min;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.jsoup.Jsoup.parse;
import static org.jsoup.helper.StringUtil.isBlank;

@Slf4j
public class YoutubeSearchParser {

    private static final Pattern SIZE_PATTERN = Pattern.compile("(Size: )(.*)( MB)");

    public static List<YoutubeSongDto> parseHtmlDocumentForSearch(String html) {
        try {
            Document document = parse(html);

            Element content = document.select("#content").get(0);
            Element results = content.select("#results").get(0);
            Element itemSection = results.select(".item-section").get(0);

            return parseItemSection(itemSection);
        } catch (Exception e) {
            log.debug("Ignored exception {}", e.getMessage());
            return newArrayList();
        }
    }

    private static List<YoutubeSongDto> parseItemSection(Element itemSection) {
        List<YoutubeSongDto> result = newArrayList();

        Elements lookupContents = itemSection.select(".yt-lockup-content");
        Elements lookupContentsWithThumbnail = itemSection.select(".yt-lockup-thumbnail");

        for (int i = 0; i < min(lookupContents.size(), lookupContentsWithThumbnail.size()); i++) {
            try {
                Element lookupContentWithThumbnail = lookupContentsWithThumbnail.get(i);

                Element thumbnailWithDuration = lookupContentWithThumbnail.select(".yt-thumb-simple").get(0);
                String thumbnailUrl = thumbnailWithDuration.select("img").attr("src");
                if (thumbnailUrl.startsWith("/")) {
                    thumbnailUrl = null;
                }
                int duration = getDurationSec(thumbnailWithDuration.select(".video-time").html());
                if (duration == 0) {
                    continue;
                }
                String videoId = lookupContentWithThumbnail.select(".yt-uix-sessionlink").attr("href");

                Element lookupContent = lookupContents.get(i);
                ArtistsTitle artistsTitle = ArtistsTitle.parse(lookupContent.select(".yt-uix-tile-link").attr("title"));

                result.add(new YoutubeSongDto(artistsTitle, duration, thumbnailUrl, videoId));
            } catch (Exception e) {
                log.debug("Ignored exception {}", e.getMessage());
            }
        }

        return result;
    }

    private static int getDurationSec(String str) {
        if (isBlank(str)) {
            return 0;
        }

        String[] minutesSeconds = str.split(":");
        if (minutesSeconds.length != 2) {
            return 0;
        }

        return 60 * valueOf(minutesSeconds[0]) + valueOf(minutesSeconds[1]);
    }

    public static Optional<Double> parseSizeMb(String html) {
        try {
            Document document = parse(html);
            final Element element = document.select(".col-md-9").get(0);
            final String sizeElementText = element.select("p").select("p:contains(Size)").get(0).text();
            final Matcher matcher = SIZE_PATTERN.matcher(sizeElementText);
            if (matcher.matches()) {
                return Optional.of(parseDouble(matcher.group(2)));
            }
        } catch (Exception e) {
            log.info("Exception parsing size from 320youtube, html {}", html, e);
        }
        return empty();
    }

    public static Optional<String> parseMp3Url(String html) {
        try {
            Document document = parse(html);
            final Element element = document.select("#download").get(0);
            return ofNullable(element.select("a").attr("href"));
        } catch (Exception e) {
            log.info("Exception parsing size from 320youtube, html {}", html, e);
        }
        return empty();
    }

}
