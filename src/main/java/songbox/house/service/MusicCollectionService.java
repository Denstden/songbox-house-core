package songbox.house.service;

import songbox.house.domain.entity.MusicCollection;
import songbox.house.domain.entity.user.UserInfo;

public interface MusicCollectionService {
    MusicCollection save(MusicCollection collection);

    MusicCollection create(String name);
    MusicCollection create(String name, UserInfo userInfo);

    MusicCollection findByName(String name);

    MusicCollection findById(Long collectionId);

    void delete(Long collectionId);

    void checkOwner(Long collectionId);

    Iterable<MusicCollection> findAll();
    Iterable<MusicCollection> findAll(String userName);
    boolean isOwner(Long collectionId, UserInfo userInfo);

    boolean exists(String name);

    MusicCollection getOrCreate(String name);
}
