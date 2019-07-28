package songbox.house.util.compare;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import songbox.house.util.compare.LevenshteinDistanceComparator;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static songbox.house.util.compare.LevenshteinDistanceComparator.MAX_COMPARE_RESULT;

@RunWith(Parameterized.class)
public class LevenshteinDistanceComparatorTest {
    private LevenshteinDistanceComparator comparator; // Tested object

    private final String word1;
    private final String word2;
    private final int expectedResult;

    public LevenshteinDistanceComparatorTest(String word1, String word2, int expectedResult) {
        this.word1 = word1;
        this.word2 = word2;
        this.expectedResult = expectedResult;
    }

    @Before
    public void init() {
        comparator = new LevenshteinDistanceComparator();
    }

    @Parameterized.Parameters
    public static Collection comparingWords() {
        return Arrays.asList(new Object[][]{
                // Remove `l` from first word(Ivan), and change `o` into `a` on second word(Brody)
                // Expected distance 2
                {"Ivan Brody", "Ival Brodsady Brady", 2},

                // All characters is distinct, so we need to remove all characters
                {"123 456 789", "abc def qwe", 9},

                // Here we need change only Martin to Ivan. M(a)rt(i)n -> (I)v(a)n, remove from the first word 4 characters
                {"Martin Dorn", "Ivan Dorn", 4},

                // We just need to change an order
                {"Albert b2b Krab", "Krab b2b Albert", 0}
        });
    }

    @Test
    public void testComparing() {
        int maxDistance = Arrays.stream(word1.split(" "))
                .map(String::length)
                .mapToInt(v -> v)
                .sum();
        System.out.println(String.format("word1 = '%s', word2 = '%s'\nexpectedResult = %d", word1, word1, expectedResult));
        assertEquals(
                (int) ((1. - (float) expectedResult / maxDistance) * MAX_COMPARE_RESULT),
                comparator.compare(word1, word2)
        );
    }
}
