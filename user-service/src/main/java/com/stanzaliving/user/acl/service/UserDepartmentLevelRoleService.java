package com.stanzaliving.user.acl.service;

import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;

import java.util.List;

public interface UserDepartmentLevelRoleService {

    List<UserDepartmentLevelRoleEntity> addRoles(String userDepartmentLevelUuid, List<String> rolesUuid);

    void revokeRoles(UserDepartmentLevelEntity userDepartmentLevelEntity, List<String> rolesUuid);

	List<UserDepartmentLevelRoleEntity> findByRoleUuid(String roleUuid);
    
}
