package songbox.house.service;

import songbox.house.domain.entity.user.UserRole;
import songbox.house.domain.entity.user.UserRole.RoleName;

public interface UserRoleService {
    UserRole createRoleIfNotExists(RoleName roleName);

    UserRole findByName(RoleName roleName);
}
