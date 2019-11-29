package com.stanzaliving.user.acl.service;

import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleRequestDto;

import java.util.List;

public interface AclUserService {
    void addRole(AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleDto);

    void revokeAllRolesOfDepartment(String userUuid, Department department);

    List<UserDeptLevelRoleDto> getUserDeptLevelRole(String userUuid);
}
