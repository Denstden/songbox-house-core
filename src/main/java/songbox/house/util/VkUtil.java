package songbox.house.util;

public final class VkUtil {
    private static final String PARTS_DESCRIPTOR = "index.m3u8";

    private VkUtil(){
    }

    public static String getPartUrl(final String indexURL, final String relativePartURL) {
        final int indexOfIndex = indexURL.indexOf(PARTS_DESCRIPTOR);
        final String mainPart = indexURL.substring(0, indexOfIndex);
        return mainPart + relativePartURL;
    }
}
