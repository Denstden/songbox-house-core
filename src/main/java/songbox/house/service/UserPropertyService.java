package songbox.house.service;

import songbox.house.domain.entity.user.UserProperty;

public interface UserPropertyService {
    UserProperty getCurrentUserProperty();
    void saveUserProperty(UserProperty userProperty);
    boolean isUseGoogleDrive();
    long getCurrentMusicCollectionId();
}
