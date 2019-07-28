package songbox.house.service;

import songbox.house.domain.entity.MusicCollection;
import songbox.house.domain.entity.user.UserInfo;

import java.util.Set;

public interface UserService {

    UserInfo findByUserName(String userName);

    UserInfo getCurrentUser();

    String getCurrentUserName();

    default boolean checkCanGet(final Set<MusicCollection> collections) {
        if (collections != null) {
            for (final MusicCollection collection : collections) {
                final UserInfo userInfo = collection.getOwner();
                if (userInfo.getUserName().equals(getCurrentUserName())) {
                    return true;
                }
            }
        }

        return false;
    }
}
