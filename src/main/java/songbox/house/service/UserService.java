package songbox.house.service;

import songbox.house.domain.dto.request.UserDto;
import songbox.house.domain.entity.user.UserInfo;
import songbox.house.domain.entity.user.UserProperty;
import songbox.house.util.ThreadLocalAuth;

import java.util.Optional;

public interface UserService {

    UserInfo findByUserName(String userName);
    UserInfo findByUserNameOrCreate(String userName);

    UserInfo createAdminIfNotExists();

    UserInfo createUser(final UserDto userDto);

    UserInfo getCurrentUser();

    String getCurrentUserName();

    Optional<UserInfo> getByTelegramId(final String telegramId);

    boolean assignTelegramId(final String login, final String password, final String telegramId);
}
