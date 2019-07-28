package songbox.house.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ResourceLoader {

    public static String loadResource(final String pathStr) throws URISyntaxException, IOException {
        final URL resource = ResourceLoader.class.getClassLoader().getResource(pathStr);
        final Path path = Paths.get(resource.toURI());
        return new String(Files.readAllBytes(path));
    }
}
