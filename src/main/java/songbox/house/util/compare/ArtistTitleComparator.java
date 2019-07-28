package songbox.house.util.compare;

import songbox.house.util.Pair;

public class ArtistTitleComparator extends LevenshteinDistanceComparator {
    public int compareArtistTitle(Pair<String, String> artistTitle1, Pair<String, String> artistTitle2) {
        return compare(artistTitle1.getLeft() + "-" + artistTitle1.getRight(),
                artistTitle2.getLeft() + "-" + artistTitle2.getRight());
    }
}
