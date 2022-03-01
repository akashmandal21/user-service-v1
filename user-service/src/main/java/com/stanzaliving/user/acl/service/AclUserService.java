package com.stanzaliving.user.acl.service;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.dto.UserAccessLevelIdsByRoleNameWithFiltersDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleListDto;
import com.stanzaliving.core.user.acl.dto.UsersByFiltersRequestDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleByEmailRequestDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleRequestDto;
import com.stanzaliving.core.user.dto.response.UserAccessModuleDto;
import com.stanzaliving.core.user.dto.response.UserContactDetailsResponseDto;

import java.util.List;
import java.util.Map;

public interface AclUserService {
	void addRole(AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleDto);

	void revokeAllRolesOfDepartment(String userUuid, Department department);

	List<UserDeptLevelRoleDto> getActiveUserDeptLevelRole(String userUuid);

	List<UserDeptLevelRoleDto> getUserDeptLevelRole(String userUuid);

	List<RoleDto> getUserRoles(String userUuid);

	void revokeAllRolesOfDepartmentOfLevel(String userUuid, Department department, AccessLevel accessLevel);

	void revokeAccessLevelEntityForDepartmentOfLevel(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto);

	void revokeRolesForDepartmentOfLevel(UserDeptLevelRoleListDto userDeptLevelRoleListDto);

    Map<String, List<String>> getUsersForRoles(Department department,String roleName,List<String> accessLevelEntity);

	Map<String, List<String>> getActiveUsersForRoles(Department department,String roleName,List<String> accessLevelEntity);

	List<UserContactDetailsResponseDto> getUserContactDetails(Department department, String roleName, List<String> accessLevelEntity);

	void bulkAddRole(AddUserDeptLevelRoleByEmailRequestDto addUserDeptLevelRoleByEmailRequestDto);

	List<UserAccessModuleDto> getUserAccessModulesByUserUuid(String userUuid);

	Map<String, List<String>> getUsersForRolesWithFilters(UserAccessLevelIdsByRoleNameWithFiltersDto userAccessLevelIdsByRoleNameWithFiltersDto);
}
