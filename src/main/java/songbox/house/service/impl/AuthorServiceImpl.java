package songbox.house.service.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songbox.house.domain.entity.Author;
import songbox.house.repository.AuthorRepository;
import songbox.house.service.AuthorService;

import java.util.Set;
import java.util.stream.Stream;

import static java.lang.Character.isLetter;
import static java.lang.Character.toUpperCase;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

@Service
@Transactional
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private static final String WHITES_REGEX = "\\s+";

    AuthorRepository authorRepository;

    @Override
    public Set<Author> getOrCreateAuthors(final Set<String> authors) {
        return authors.stream()
                .map(String::trim)
                .map(this::getOrCreate)
                .collect(toSet());
    }

    private Author getOrCreate(final String authorStr) {
        final String normalized = normalizeAuthorName(authorStr);
        final Author fromDb = authorRepository.findByName(normalized);

        return ofNullable(fromDb)
                .orElseGet(() -> authorRepository.save(new Author(normalized)));
    }

    private String normalizeAuthorName(final String authorName) {
        final String[] words = authorName.split(WHITES_REGEX);
        final StringBuilder builder = new StringBuilder();

        Stream.of(words)
                .filter(StringUtils::isNotEmpty)
                .forEach(word -> processWord(builder, word));

        return builder.toString().trim();
    }

    private void processWord(final StringBuilder builder, final String word) {
        final char first = word.charAt(0);
        if (isLetter(first)) {
            builder.append(toUpperCase(word.charAt(0)))
                    .append(word.substring(1).trim());
        } else {
            builder.append(word.trim());
        }

        builder.append(" ");
    }
}
