package songbox.house.util.compare;

import java.util.Comparator;

public class BitRateDurationComparator implements Comparator<BitRateDuration> {
    @Override
    public int compare(BitRateDuration song1, BitRateDuration song2) {
        final int compareBitRates = (song1.getBitRate() == null || song2.getBitRate() == null) ? 0 :
                song1.getBitRate().compareTo(song2.getBitRate());
        if (compareBitRates != 0) {
            return compareBitRates;
        } else {
            return (song1.getDuration() == null || song2.getDuration() == null) ? 0 :
                    song1.getDuration().compareTo(song2.getDuration());
        }
    }
}
