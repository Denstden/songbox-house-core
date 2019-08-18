package songbox.house.service;

import songbox.house.domain.entity.user.UserProperty;

public interface UserPropertyService {
    UserProperty getUserProperty();
    void saveUserProperty(UserProperty userProperty);
}
