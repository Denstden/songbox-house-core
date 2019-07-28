package songbox.house.util;

import org.junit.Test;
import songbox.house.domain.dto.request.TrackListParsingResultDto;
import songbox.house.util.parser.TrackListParser;

import static org.junit.Assert.assertEquals;


public class TrackListParserTest {
    @Test
    public void shouldParseTrackList() {
        final String trackList = "1. Gestalt - Mindfck - unreleased\n" +
                "2. Benn Rogue - Subdued Pain - unreleased\n" +
                "3. Identified Patient - Unable to Relax - unreleased\n" +
                "4. ? - forthcoming Mechatronica\n" +
                "5. Privacy - Shove - Klakson\n" +
                "6. Dexter vs. Cosmic Force - Geheugen Meuk - Marguerita Recordings\n" +
                "7. The Hacker - Dans La Salle Des Machines - Datapunk\n" +
                "8. Reckless Ron Cook - Electro Soul, Ride It - 7 Days Ent.\n" +
                "9. Unit Trax - Asert Teeth - unreleased\n" +
                "10. Dez Williams - Carkrash Vikdim - forthcoming Mechatronica\n" +
                "11. AS1 - Superior Robots - Last Known Trajectory\n" +
                "12. Computor Rockers - Green Screen (DMX Krew Remix) - Breakin’ Records\n" +
                "13. Daddy Long Legs - The Club - Central Processing Unit\n" +
                "14. Sync 24 & Jensen Interceptor - Wave Id - Cultivated Electronics\n" +
                "15. Dez Williams - Xen - forthcoming Mechatronica\n" +
                "16. Dj Nephil - Supernova - Gravitational Waves\n" +
                "17. Far Electronics - Inner Language - Omnidisc\n" +
                "18. Benedikt Frey - Sh Birds - Live At Robert Johnson\n" +
                "19. A Sagittariun - Gravitational Push - Teal Recordings";

        final String regex = "(?<id>\\d+\\. )(?<artists>.*)( - )(?<title>.*)( - )(?<label>.*)";

        final TrackListParsingResultDto result = TrackListParser.parseTrackList(trackList, regex, "\n");

        assertEquals(18, result.getArtistTitles().size());
        assertEquals(1, result.getNotParsed().size());
    }

    @Test
    public void shouldParse2() {
        final String trackList = "1) Gestalt - Mindfck \n" +
                "2) Benn Rogue - Subdued Pain\n";

        final String regex = "(?<id>\\d+\\) )(?<artists>.*)( - )(?<title>.*)";

        final TrackListParsingResultDto result = TrackListParser.parseTrackList(trackList, regex, "\n");

        assertEquals(2, result.getArtistTitles().size());
    }
}