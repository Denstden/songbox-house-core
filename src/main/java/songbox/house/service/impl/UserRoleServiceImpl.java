package songbox.house.service.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songbox.house.domain.entity.user.UserRole;
import songbox.house.exception.NotExistsException;
import songbox.house.repository.UserRoleRepository;
import songbox.house.service.UserRoleService;

import java.text.MessageFormat;

@Service
@Transactional
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    UserRoleRepository roleRepository;

    @Override
    public UserRole createRoleIfNotExists(final String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> createAndSave(roleName));
    }

    private UserRole createAndSave(final String roleName) {
        final UserRole role = create(roleName);
        return roleRepository.save(role);
    }

    @Override
    public UserRole findByName(final String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new NotExistsException(MessageFormat.format("Role with name {0} not exists!", roleName)));
    }

    private UserRole create(final String roleName) {
        final UserRole role = new UserRole();
        role.setName(roleName);
        return role;
    }
}
