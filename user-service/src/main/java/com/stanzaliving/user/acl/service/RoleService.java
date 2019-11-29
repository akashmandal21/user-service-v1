package com.stanzaliving.user.acl.service;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.request.dto.AddRoleRequestDto;

import java.util.List;

public interface RoleService {
    RoleDto addRole(AddRoleRequestDto addRoleRequestDto);

    RoleDto getRoleByUuid(String roleUuid);

    List<RoleDto> findByDepartmentAndAccessLevel(Department department, AccessLevel accessLevel);
}
