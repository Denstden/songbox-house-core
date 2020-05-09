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

import java.util.Set;

import static java.text.MessageFormat.format;
import static java.util.Optional.ofNullable;
import static org.springframework.util.CollectionUtils.isEmpty;
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

        final MusicCollection collection = createCollectionWithName(name);

        return repository.save(collection);
    }

    @Override
    public MusicCollection findByName(final String name) {
        checkOwner(name);

        return repository.findByCollectionNameIgnoreCase(name);
    }

    @Override
    public MusicCollection findById(final Long collectionId) {
        checkOwner(collectionId);

        return repository.findById(collectionId)
                .orElseGet(this::getOrCreateDefault);
    }

    @Override
    public MusicCollection findById(Long collectionId, Long ownerId) {
        final String ownerUserName = userService.findById(ownerId).getUserName();
        checkOwner(collectionId, ownerUserName);

        return ofNullable(collectionId)
                .flatMap(repository::findById)
                .orElseGet(this::getOrCreateDefault);
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
    public boolean exists(final String name) {
        return repository.findByCollectionNameIgnoreCase(name) != null;
    }

    @Override
    public MusicCollection getOrCreate(String name) {
        return ofNullable(repository.findByCollectionNameIgnoreCase(name))
                .orElseGet(() -> create(name));
    }

    @Override
    public boolean checkCanGet(final Set<MusicCollection> collections) {
        if (isEmpty(collections)) {
            return false;
        }

        return collections.stream()
                .map(MusicCollection::getOwner)
                .map(UserInfo::getName)
                .anyMatch(userService.getCurrentUserName()::equalsIgnoreCase);
    }

    @Override
    public MusicCollection getOrCreateDefault() {
        return getOrCreate("Default Collection " + userService.getCurrentUserName().hashCode());
    }

    @Override
    public void checkOwner(final Long collectionId) {
        final MusicCollection collection = ofNullable(collectionId)
                .flatMap(repository::findById)
                .orElseGet(this::getOrCreateDefault);

        checkOwner(collection);
    }


    private void checkOwner(final String collectionName) {
        final MusicCollection collection = repository.findByCollectionNameIgnoreCase(collectionName);

        if (collection == null) {
            throw new NotExistsException(format("Collection with name {0} not exists", collectionName));
        }

        checkOwner(collection);
    }

    private void checkOwner(final Long collectionId, String ownerUserName) {
        final MusicCollection collection = ofNullable(collectionId)
                .flatMap(repository::findById)
                .orElseGet(this::getOrCreateDefault);

        checkOwner(collection, ownerUserName);
    }

    private void checkOwner(final MusicCollection collection) {
        final String currentUserName = userService.getCurrentUserName();
        checkOwner(collection, currentUserName);
    }

    private void checkOwner(final MusicCollection collection, String userName) {
        log.trace("User \"{}\"", userName);
        final UserInfo owner = collection.getOwner();
        log.trace("Collection owner \"{}\"", owner.getUserName());

        if (!userName.equals(owner.getUserName()) && !ADMIN.equals(owner.getRole().getName())) {
            throw new AccessDeniedException(format("User {0} hasn't access to collection with name {1}",
                    userName, collection.getCollectionName()));
        }
    }

    private MusicCollection createCollectionWithName(final String name) {
        final MusicCollection collection = new MusicCollection(name);
        final String currentUserName = userService.getCurrentUserName();
        final UserInfo owner = userService.findByUserName(currentUserName);
        collection.setOwner(owner);
        return collection;
    }
}