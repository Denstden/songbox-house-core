package songbox.house.service.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import songbox.house.domain.entity.Genre;
import songbox.house.exception.ExistsException;
import songbox.house.repository.GenreRepository;
import songbox.house.service.GenreService;

import java.util.Optional;

import static songbox.house.util.StringUtils.firstCapitalLetter;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class GenreServiceImpl implements GenreService {
    GenreRepository repository;

    //    @PostConstruct
    public void init() {
        create("Electro");
        create("Acid");
        create("Techno");
        create("EBM");
        create("Industrial");
        create("Minimal");
        create("House");
        create("Tech house");
        create("Deep house");
        create("Breaks");
    }

    @Override
    public Genre create(final String name) {
        final String normalized = firstCapitalLetter(name);

        if (exists(normalized)) {
            throw new ExistsException("Genre with name \"" + normalized + "\" already exists.");
        }

        final Genre genre = new Genre(normalized);

        return repository.save(genre);
    }

    @Override
    public Genre findByName(final String name) {
        return repository.findByName(name);
    }

    @Override
    public Iterable<Genre> findAll() {
        return repository.findAll();
    }

    @Override
    public Genre getOrCreate(String genreName) {
        return Optional.ofNullable(repository.findByName(genreName))
                .orElseGet(() -> new Genre(genreName));
    }

    private boolean exists(final String name) {
        return repository.findByName(name) != null;
    }
}
