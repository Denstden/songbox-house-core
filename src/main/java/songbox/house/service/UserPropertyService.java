package songbox.house.service;

import songbox.house.domain.entity.user.UserProperty;
import songbox.house.domain.entity.user.UserPropertyMask;

import java.util.Collection;

public interface UserPropertyService {
    UserProperty getCurrentUserProperty();
    void saveUserProperty(UserProperty userProperty);
    boolean isUseGoogleDrive();
    long getCurrentMusicCollectionId();
    void setMasks(UserProperty userProperty, Collection<UserPropertyMask> masks);
    void removeMasks(UserProperty userProperty, Collection<UserPropertyMask> masks);
}
