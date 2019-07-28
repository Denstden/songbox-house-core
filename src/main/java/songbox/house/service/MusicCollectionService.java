package songbox.house.service;

import songbox.house.domain.entity.MusicCollection;

public interface MusicCollectionService {
    MusicCollection save(MusicCollection collection);

    MusicCollection create(String name);

    MusicCollection findByName(String name);

    MusicCollection findById(Long collectionId);

    void delete(Long collectionId);

    void checkOwner(Long collectionId);

    Iterable<MusicCollection> findAll();

    boolean exists(String name);

    MusicCollection getOrCreate(String name);
}
