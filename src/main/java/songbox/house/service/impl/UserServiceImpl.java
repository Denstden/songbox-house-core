package songbox.house.service.impl;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songbox.house.domain.dto.request.UserDto;
import songbox.house.domain.entity.user.UserInfo;
import songbox.house.domain.entity.user.UserRole;
import songbox.house.exception.NotExistsException;
import songbox.house.repository.UserInfoRepository;
import songbox.house.service.CurrentUserService;
import songbox.house.service.UserRoleService;
import songbox.house.service.UserService;

import java.util.Optional;

import static java.text.MessageFormat.format;
import static songbox.house.domain.entity.user.UserRole.RoleName.ADMIN;
import static songbox.house.domain.entity.user.UserRole.RoleName.USER;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserInfoRepository userInfoRepository;
    private final UserRoleService userRoleService;
    private final CurrentUserService currentUserService;
    private final StrongPasswordEncryptor strongPasswordEncryptor;

    private final String adminPassword;
    private final String adminUserName;

    @Autowired
    public UserServiceImpl(final UserInfoRepository userInfoRepository, final UserRoleService userRoleService,
            final CurrentUserService currentUserService, final StrongPasswordEncryptor strongPasswordEncryptor,
            @Value("${songbox.house.admin.password}") final String adminPassword,
            @Value("${songbox.house.admin.username}") final String adminUserName) {
        this.userInfoRepository = userInfoRepository;
        this.userRoleService = userRoleService;
        this.currentUserService = currentUserService;
        this.strongPasswordEncryptor = strongPasswordEncryptor;
        this.adminUserName = adminUserName;
        this.adminPassword = adminPassword;
    }

    @Override
    public UserInfo createAdminIfNotExists() {
        final Optional<UserInfo> adminOpt = userInfoRepository.findByUserName(adminUserName);

        if (adminOpt.isPresent()) {
            final UserInfo admin = adminOpt.get();
            setRequiredAdminFields(admin);

            return admin;
        } else {
            final UserInfo admin = createAdmin();
            return userInfoRepository.save(admin);
        }
    }

    @Override
    public UserInfo createUser(final UserDto userDto) {
        final UserInfo userInfo = new UserInfo();

        userInfo.setUserName(userDto.getUserName());
        userInfo.setPassword(strongPasswordEncryptor.encryptPassword(userDto.getPassword()));
        userInfo.setActive(true);
        userInfo.setEmail(userDto.getEmail());
        userInfo.setName(userDto.getName());
        userInfo.setSurname(userDto.getSurname());
        userInfo.setRole(userRoleService.findByName(USER));

        return userInfoRepository.save(userInfo);
    }

    @Override
    public UserInfo getCurrentUser() {
        return findByUserName(getCurrentUserName());
    }

    @Override
    public String getCurrentUserName() {
        return currentUserService.getCurrentUserName();
    }

    @Override
    public Optional<UserInfo> getByTelegramId(String telegramId) {
        return userInfoRepository.findByTelegramId(telegramId);
    }

    @Override
    public boolean assignTelegramId(String login, String password, String telegramId) {
        final UserInfo user = findByUserName(login);
        final String userPassword = user.getPassword();
        if (strongPasswordEncryptor.checkPassword(password, userPassword)) {
            user.setTelegramId(telegramId);
            userInfoRepository.save(user);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public UserInfo findByUserName(final String userName) {
        return userInfoRepository.findByUserName(userName)
                .orElseThrow(() -> new NotExistsException(format("User with name {0} not exists.", userName)));
    }

    private void setRequiredAdminFields(final UserInfo admin) {
        admin.setActive(true);
        admin.setPassword(strongPasswordEncryptor.encryptPassword(adminPassword));

        final UserRole adminRole = userRoleService.createRoleIfNotExists(ADMIN);
        admin.setRole(adminRole);
    }

    private UserInfo createAdmin() {
        final UserInfo admin = new UserInfo();

        admin.setUserName(adminUserName);
        setRequiredAdminFields(admin);

        return admin;
    }
}
