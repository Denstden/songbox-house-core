package songbox.house.service.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songbox.house.domain.entity.MusicCollection;
import songbox.house.domain.entity.user.UserInfo;
import songbox.house.exception.AccessDeniedException;
import songbox.house.exception.ExistsException;
import songbox.house.exception.NotExistsException;
import songbox.house.repository.MusicCollectionRepository;
import songbox.house.service.MusicCollectionService;
import songbox.house.service.UserService;

import static java.text.MessageFormat.format;
import static java.util.Optional.ofNullable;
import static songbox.house.domain.entity.user.UserRole.RoleName.ADMIN;

@Service
@Transactional
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class MusicCollectionServiceImpl implements MusicCollectionService {

    MusicCollectionRepository repository;
    UserService userService;

    @Override
    public MusicCollection save(final MusicCollection collection) {
        return repository.save(collection);
    }

    @Override
    public MusicCollection create(final String name) {
        if (exists(name)) {
            throw new ExistsException("Music collection with name \"" + name + "\" already exists.");
        }

        final MusicCollection collection = createCollectionWithName(name, userService.getCurrentUser());

        return repository.save(collection);
    }

    @Override
    public MusicCollection create(String name, UserInfo userInfo) {
        return repository.save(createCollectionWithName(name, userInfo));
    }

    private MusicCollection createCollectionWithName(final String name, UserInfo owner) {
        final MusicCollection collection = new MusicCollection(name);
        collection.setOwner(owner);
        return collection;
    }

    @Override
    public MusicCollection findByName(final String name) {
        checkOwner(name);

        return repository.findByCollectionNameIgnoreCase(name);
    }

    @Override
    public MusicCollection findById(final Long collectionId) {
        checkOwner(collectionId);

        return repository.findById(collectionId).orElse(null);
    }

    @Override
    public void delete(final Long collectionId) {
        checkOwner(collectionId);

        repository.deleteById(collectionId);
    }

    @Override
    public Iterable<MusicCollection> findAll() {
        final String currentUserName = userService.getCurrentUserName();
        return repository.findByOwner_UserName(currentUserName);
    }

    @Override
    public Iterable<MusicCollection> findAll(String userName) {
        return repository.findByOwner_UserName(userName);
    }

    @Override
    public boolean isOwner(Long collectionId, UserInfo userInfo) {
        MusicCollection collection = findById(collectionId);
        if (collection == null) {
            return false;
        }

        return collection.getOwner().equals(userInfo);
    }

    @Override
    public boolean exists(final String name) {
        return repository.findByCollectionNameIgnoreCase(name) != null;
    }

    @Override
    public MusicCollection getOrCreate(String name) {
        return ofNullable(repository.findByCollectionNameIgnoreCase(name))
                .orElseGet(() -> create(name));
    }

    private void checkOwner(final String collectionName) {
        final MusicCollection collection = repository.findByCollectionNameIgnoreCase(collectionName);

        if (collection == null) {
            throw new NotExistsException(format("Collection with name {0} not exists", collectionName));
        }

        checkOwner(collection);
    }

    @Override
    public void checkOwner(final Long collectionId) {
        final MusicCollection collection = repository.findById(collectionId)
                .orElseThrow(() -> new NotExistsException(format("Collection with id {0} not exists", collectionId)));

        checkOwner(collection);
    }

    private void checkOwner(final MusicCollection collection) {
        // Currently disabled ( get current User not works in telegram case)
        if (true) {
            return;
        }

        final String currentUserName = userService.getCurrentUserName();
        log.trace("Current user \"{}\"", currentUserName);
        final UserInfo owner = collection.getOwner();
        log.trace("Collection owner \"{}\"", owner.getUserName());

        if (!currentUserName.equals(owner.getUserName()) && !ADMIN.equals(owner.getRole().getName())) {
            throw new AccessDeniedException(format("User {0} hasn't access to collection with name {1}",
                    currentUserName, collection.getCollectionName()));
        }
    }
}
