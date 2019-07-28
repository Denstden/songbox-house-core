package songbox.house.service;

import songbox.house.domain.entity.Author;

import java.util.Set;

public interface AuthorService {
    Set<Author> getOrCreateAuthors(final Set<String> authors);
}
