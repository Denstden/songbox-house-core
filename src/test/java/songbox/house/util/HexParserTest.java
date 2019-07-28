package songbox.house.util;

import org.junit.Test;
import songbox.house.util.Pair;
import songbox.house.util.parser.HexParser;

import java.util.Optional;

import static org.junit.Assert.*;

public class HexParserTest {

    @Test
    public void shouldParseHex() {
        //given
        final String hex = "b0711ec668cf5b477c//2d69f07a5783ad6b7d///b199b88952e065eb84";

        //when
        Optional<Pair<String, String>> result = HexParser.parseHex(hex);

        //then
        Pair<String, String> pair = result.get();
        assertEquals("2d69f07a5783ad6b7d", pair.getLeft());
        assertEquals("b199b88952e065eb84", pair.getRight());
    }
}