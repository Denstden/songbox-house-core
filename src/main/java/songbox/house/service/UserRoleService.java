package songbox.house.service;

import songbox.house.domain.entity.user.UserRole;

public interface UserRoleService {
    UserRole createRoleIfNotExists(String roleName);

    UserRole findByName(String roleName);
}
