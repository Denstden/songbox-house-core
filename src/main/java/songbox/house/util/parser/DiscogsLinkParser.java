package songbox.house.util.parser;

import songbox.house.exception.InvalidDiscogsLinkException;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static songbox.house.service.DiscogsFacade.*;
import static songbox.house.service.DiscogsFacade.ARTIST_DETERMINER;
import static songbox.house.service.DiscogsFacade.DATABASE_DETERMINER;
import static songbox.house.service.DiscogsFacade.LABEL_DETERMINER;
import static songbox.house.service.DiscogsFacade.MARKETPLACE_DETERMINER;


public class DiscogsLinkParser {

    public static Long parseAsRelease(final String link) {
        return parse(link, DATABASE_DETERMINER);
    }

    public static Long parseAsMarketplaceItem(final String link) {
        return parse(link, MARKETPLACE_DETERMINER);
    }

    public static Long parseAsLabel(final String link) {
        return parse(link, LABEL_DETERMINER);
    }

    public static Long parseAsArtist(final String link) {
        return parse(link, ARTIST_DETERMINER);
    }

    private static Long parse(final String link, final String determiner) {
        final int indexOfRelease = link.indexOf(determiner);

        if (indexOfRelease > 0) {
            final String id = link.substring(indexOfRelease + determiner.length() + 1);
            return getNumber(id);
        } else {
            throw new InvalidDiscogsLinkException(MessageFormat.format("Can't parse link \"{0}\". Link should contain \"{1}\" word", link, determiner));
        }
    }

    private static Long getNumber(final String str) {
        final Matcher matcher = Pattern.compile("\\d+").matcher(str);
        matcher.find();
        return Long.valueOf(matcher.group());
    }
}
