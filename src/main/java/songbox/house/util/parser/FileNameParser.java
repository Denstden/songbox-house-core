package songbox.house.util.parser;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import songbox.house.domain.FileNameData;
import songbox.house.util.Pair;

import java.util.HashSet;
import java.util.Set;

import static songbox.house.util.StringUtils.parseAuthors;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Slf4j
@Component
public class FileNameParser {
    private static final String FILE_NAME_EXTENSION_SEPARATOR = ".";
    private static final String EMPTY_EXTENSION = "";
    private static final String DASH = "-";

    public FileNameData parseFileName(final String fileName) {
        final Pair<String, String> nameExtension = parseNameExtension(fileName);
        final Pair<String, Set<String>> titleAuthors = parseTitleAuthors(nameExtension.getLeft());

        return new FileNameData(titleAuthors.getLeft().trim(), titleAuthors.getRight(), nameExtension.getRight().trim());
    }

    private Pair<String, String> parseNameExtension(final String fileName) {
        final int indexOfStartExtension = fileName.lastIndexOf(FILE_NAME_EXTENSION_SEPARATOR);

        if (indexOfStartExtension < 0) {
            log.warn("Cant parse extension from filename {}", fileName);
            return new Pair<>(fileName, EMPTY_EXTENSION);
        } else {
            final String title = fileName.substring(0, indexOfStartExtension);
            final String extension = fileName.substring(indexOfStartExtension + 1);
            return new Pair<>(title, extension);
        }
    }

    //TODO correct processing of Author - Title (author remix)
    private Pair<String, Set<String>> parseTitleAuthors(final String fileNameWithoutExtension) {
        final String normalized = normalize(fileNameWithoutExtension);

        final int indexOfDash = normalized.lastIndexOf(DASH);

        if (indexOfDash < 0) {
            return new Pair<>(fileNameWithoutExtension, new HashSet<>());
        } else {
            final String authorsString = fileNameWithoutExtension.substring(0, indexOfDash);
            final String title = fileNameWithoutExtension.substring(indexOfDash + 1);

            final Set<String> authors = parseAuthors(authorsString);

            return new Pair<>(title, authors);
        }
    }

    private String normalize(final String fileNameWithoutExtension) {
        String normalized = fileNameWithoutExtension.trim();

        if (fileNameWithoutExtension.endsWith(DASH)) {
            normalized = fileNameWithoutExtension.substring(0, fileNameWithoutExtension.length() - 1);
        }

        return normalized;
    }
}
