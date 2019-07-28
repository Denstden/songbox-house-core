package songbox.house.util;

import org.junit.Test;
import songbox.house.util.parser.DiscogsLinkParser;

import static org.junit.Assert.*;

public class DiscogsLinkParserTest {

    @Test
    public void shouldParseReleaseLink() {
        final String link = "https://www.discogs.com/Artwork-Let-Go-Of-This-Acid/release/10890370?ev=item-vc";

        final Long result = DiscogsLinkParser.parseAsRelease(link);

        assertEquals(10890370L, (long) result);
    }

    @Test
    public void shouldParseMarketplaceItemLink() {
        final String link = "https://www.discogs.com/sell/item/732648488";

        final Long result = DiscogsLinkParser.parseAsMarketplaceItem(link);

        assertEquals(732648488L, (long) result);
    }
}