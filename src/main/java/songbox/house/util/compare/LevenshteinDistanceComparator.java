package songbox.house.util.compare;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LevenshteinDistanceComparator implements Comparator<String> {

    private static final String SPLIT_PATTERN = "[\\p{Punct}\\s]+";
    public static final int MAX_COMPARE_RESULT = 100;

    /**
     * Edit aka Levenshtein distance
     *
     * @param wordA first word
     * @param wordB second word
     * @return minimum number of insertions, deletions or substitutions to change one word into another
     */
    private static int distance(String wordA, String wordB) {
        wordA = wordA.toLowerCase();
        wordB = wordB.toLowerCase();

        int[][] costs = new int[wordA.length() + 1][wordB.length() + 1];

        // Init
        for (int i = 0; i <= wordA.length(); i++) {
            costs[i][0] = i;
        }

        for (int j = 0; j <= wordB.length(); j++) {
            costs[0][j] = j;
        }

        for (int i = 0; i < wordA.length(); i++) {
            char charWordA = wordA.charAt(i);
            for (int j = 0; j < wordB.length(); j++) {
                char charWordB = wordB.charAt(j);

                if (charWordA == charWordB) {
                    costs[i + 1][j + 1] = costs[i][j];
                } else {
                    int replaceCost = costs[i][j] + 1;
                    int insertCost = costs[i][j + 1] + 1;
                    int deleteCost = costs[i + 1][j] + 1;

                    int minCost = replaceCost > insertCost ? insertCost : replaceCost;
                    minCost = deleteCost > minCost ? minCost : deleteCost;
                    costs[i + 1][j + 1] = minCost;
                }
            }
        }

        return costs[wordA.length()][wordB.length()];
    }

    @Override
    public int compare(String str1, String expected) {
        final String[] words1 = str1.split(SPLIT_PATTERN);
        final List<String> expectedWords = Arrays.stream(expected.split(SPLIT_PATTERN))
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        int maxDistance = 0;
        int dist = 0;
        for (String word : words1) {
            dist += expectedWords
                    .stream()
                    .map(e -> distance(word, e))
                    .mapToInt(e -> e)
                    .min()
                    .orElse(0);
            maxDistance += word.length();
        }

        // And normalize it. It will be 100 if str1 == expected, or 0 if str1 not matching expected at all
        return (int) ((1. - (float) dist / maxDistance) * MAX_COMPARE_RESULT);
    }
}