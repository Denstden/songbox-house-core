package songbox.house.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import songbox.house.domain.entity.user.UserInfo;
import songbox.house.domain.entity.user.UserProperty;
import songbox.house.repository.UserInfoRepository;
import songbox.house.repository.UserPropertyRepository;
import songbox.house.service.CurrentUserService;
import songbox.house.service.UserPropertyService;
import songbox.house.service.UserService;

@Service
@AllArgsConstructor
public class UserPropertyServiceImpl implements UserPropertyService {
    private final UserInfoRepository userInfoRepository;
    private final UserPropertyRepository userPropertyRepository;
    private final UserService userService;

    @Override
    public UserProperty getUserProperty() {
        UserInfo currentUser = userService.getCurrentUser();
        UserProperty userProperty = currentUser.getUserProperty();

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

}
