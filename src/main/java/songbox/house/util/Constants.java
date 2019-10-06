package songbox.house.util;

import org.slf4j.Marker;

import java.net.URI;

import static org.slf4j.MarkerFactory.getMarker;

public final class Constants {
    public static final String APP_NAME = "songbox-house";
    public static final String JSON_DELIMITER = "<!json>";
    public static final String JSON_DELIMITER_V2 = "<!--";
    public static final URI EMPTY_URI = URI.create("");

    public static final Marker PERFORMANCE_MARKER = getMarker("PERFORMANCE");
}
