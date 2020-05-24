package songbox.house.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import songbox.house.domain.entity.MusicCollection;
import songbox.house.domain.entity.user.UserInfo;
import songbox.house.domain.entity.user.UserProperty;
import songbox.house.domain.entity.user.UserPropertyMask;
import songbox.house.repository.UserInfoRepository;
import songbox.house.repository.UserPropertyRepository;
import songbox.house.service.MusicCollectionService;
import songbox.house.service.UserPropertyService;
import songbox.house.service.UserService;

import java.util.Collection;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static songbox.house.util.MaskUtil.removeMask;
import static songbox.house.util.MaskUtil.setMask;

@Service
@AllArgsConstructor
public class UserPropertyServiceImpl implements UserPropertyService {
    private final UserInfoRepository userInfoRepository;
    private final UserPropertyRepository userPropertyRepository;
    private final UserService userService;
    private final MusicCollectionService musicCollectionService;

    @Override
    public UserProperty getCurrentUserProperty() {
        UserInfo currentUser = userService.getCurrentUser();
        UserProperty userProperty = currentUser.getUserProperty();

        //TODO remove after clearing DB
        if (currentUser.getUserProperty() == null) {
            userProperty = new UserProperty();
            currentUser.setUserProperty(userProperty);
            userInfoRepository.save(currentUser);
        }
        return userProperty;
    }

    @Override
    public void saveUserProperty(UserProperty userProperty) {
        userPropertyRepository.save(userProperty);
    }

    @Override
    public boolean isUseGoogleDrive() {
        return isTrue(getCurrentUserProperty().getTelegramBotUseGoogleDrive());
    }

    @Override
    public long getCurrentMusicCollectionId() {
        return ofNullable(getCurrentUserProperty().getDefaultCollection())
                .map(MusicCollection::getCollectionId)
                .orElseGet(() -> musicCollectionService.getOrCreateDefault().getCollectionId());
    }

    @Override
    public void setMasks(UserProperty userProperty, Collection<UserPropertyMask> masks) {
        Long currentMask = userProperty.getMask();
        masks.forEach(userPropertyMask -> setMask(currentMask, userPropertyMask));
        saveUserProperty(userProperty);
    }

    @Override
    public void removeMasks(UserProperty userProperty, Collection<UserPropertyMask> masks) {
        Long currentMask = userProperty.getMask();
        masks.forEach(userPropertyMask -> removeMask(currentMask, userPropertyMask));
        saveUserProperty(userProperty);
    }

}
