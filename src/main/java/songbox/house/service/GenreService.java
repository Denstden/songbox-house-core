package songbox.house.service;

import songbox.house.domain.entity.Genre;

public interface GenreService {
    Genre create(String name);

    Genre findByName(String name);

    Iterable<Genre> findAll();

    Genre getOrCreate(final String genreName);
}
