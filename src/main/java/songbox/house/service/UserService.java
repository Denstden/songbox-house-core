package songbox.house.service;

import songbox.house.domain.dto.request.UserDto;
import songbox.house.domain.entity.MusicCollection;
import songbox.house.domain.entity.user.UserInfo;

import java.util.Optional;
import java.util.Set;

public interface UserService {

    UserInfo findByUserName(final String userName);

    UserInfo createAdminIfNotExists();

    UserInfo createUser(final UserDto userDto);

    boolean checkCanGet(final Set<MusicCollection> collections);

    UserInfo getCurrentUser();

    String getCurrentUserName();

    Optional<UserInfo> getByTelegramId(final String telegramId);

    boolean assignTelegramId(final String login, final String password, final String telegramId);
}
