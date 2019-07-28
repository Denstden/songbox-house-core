package songbox.house.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import songbox.house.service.UserRoleService;
import songbox.house.service.UserService;

import java.util.stream.Stream;

import static java.util.stream.Stream.of;
import static songbox.house.domain.entity.user.UserRole.RoleName.values;

@Component
public class InitDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;

    private final UserRoleService roleService;
    private final UserService userService;

    @Autowired
    public InitDataLoader(final UserRoleService roleService, final UserService userService) {
        this.roleService = roleService;
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        if (alreadySetup)
            return;

        of(values()).forEach(roleService::createRoleIfNotExists);

        userService.createAdminIfNotExists();

        alreadySetup = true;
    }
}
