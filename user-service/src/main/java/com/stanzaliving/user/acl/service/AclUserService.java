package com.stanzaliving.user.acl.service;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleListDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleRequestDto;

import java.util.List;

public interface AclUserService {
    void addRole(AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleDto);

    void revokeAllRolesOfDepartment(String userUuid, Department department);

    List<UserDeptLevelRoleDto> getActiveUserDeptLevelRole(String userUuid);

    List<UserDeptLevelRoleDto> getUserDeptLevelRole(String userUuid);

    void revokeAllRolesOfDepartmentOfLevel(String userUuid, Department department, AccessLevel accessLevel);

    void revokeAccessLevelEntityForDepartmentOfLevel(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto);

    void revokeRolesForDepartmentOfLevel(UserDeptLevelRoleListDto userDeptLevelRoleListDto);
    
    List<String> getUsersForRoles(Department department,String roleName,String accessLevelEntity);
}
