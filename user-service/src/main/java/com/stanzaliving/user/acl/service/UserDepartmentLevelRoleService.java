package com.stanzaliving.user.acl.service;

import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;

import java.util.List;

public interface UserDepartmentLevelRoleService {
    List<UserDepartmentLevelRoleEntity> saveAll(List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList);
}
