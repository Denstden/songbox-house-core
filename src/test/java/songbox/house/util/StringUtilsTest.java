package songbox.house.util;

import org.junit.Test;
import songbox.house.util.StringUtils;

import java.util.Set;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.*;
import static songbox.house.util.StringUtils.parseAuthors;

public class StringUtilsTest {

    @Test
    public void shouldRemoveEndingDigitsInBrackets() {
        final String input = "Privacy (31)";

        final String result = StringUtils.removeEndingNumberInBrackets(input);

        assertEquals("Privacy", result);
    }

    @Test
    public void shouldReturnInput() {
        final String input = "Privacy ";

        final String result = StringUtils.removeEndingNumberInBrackets(input);

        assertEquals("Privacy", result);
    }

    @Test
    public void shouldParseCorrectlyEscapedAmpersand() {
        // Given
        final String authorsStr = "Luke Eargoggle &amp; Johan Inkinen";

        // When
        final Set<String> authors = parseAuthors(authorsStr);

        // Then
        assertEquals(2, authors.size());
        assertThat(authors, containsInAnyOrder("Luke Eargoggle", "Johan Inkinen"));
    }

    @Test
    public void shouldParseCorrectlyRegularAmpersand() {
        // Given
        final String authorsStr = "Luke Eargoggle & Johan Inkinen";

        // When
        final Set<String> authors = parseAuthors(authorsStr);

        // Then
        assertEquals(2, authors.size());
        assertThat(authors, containsInAnyOrder("Luke Eargoggle", "Johan Inkinen"));
    }

    @Test
    public void shouldParseCorrectlyWrapped() {
        // Given
        final String authorsStr = "[ Luke Eargoggle ]";

        // When
        final Set<String> authors = parseAuthors(authorsStr);

        // Then
        assertEquals(1, authors.size());
        assertThat(authors, containsInAnyOrder("Luke Eargoggle"));
    }

    @Test
    public void shouldParseCorrectlyVs() {
        // Given
        final String authorsStr = "Dexter vs. Cosmic Force vs Some artist";

        // When
        final Set<String> authors = parseAuthors(authorsStr);

        // Then
        assertEquals(3, authors.size());
        assertThat(authors, containsInAnyOrder("Dexter", "Cosmic Force", "Some artist"));
    }

    @Test
    public void shouldParseCorrectlyFtFeat() {
        // Given
        final String authorsStr = "dynamic duo ft. shaquan feat Some artist";

        // When
        final Set<String> authors = parseAuthors(authorsStr);

        // Then
        assertEquals(3, authors.size());
        assertThat(authors, containsInAnyOrder("dynamic duo", "shaquan", "Some artist"));
    }
}