/**
 * 
 */
package com.stanzaliving.user.service;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.user.dto.AccessLevelRoleRequestDto;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.dto.UserManagerAndRoleDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.core.user.request.dto.UpdateDepartmentUserTypeDto;
import com.stanzaliving.core.user.request.dto.UpdateUserRequestDto;
import com.stanzaliving.user.entity.UserEntity;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface UserService {

	UserProfileDto getActiveUserByUserId(String userId);

	void assertActiveUserByUserUuid(String userId);

	UserDto addUser(AddUserRequestDto addUserRequestDto);

	UserProfileDto getUserProfile(String userId);

	PageResponse<UserProfileDto> searchUser(UserFilterDto userFilterDto);

	Map<String, UserProfileDto> getUserProfileIn(Map<String, String> userManagerUuidMap);

	boolean updateUserStatus(String userId, Boolean status);

	UserManagerAndRoleDto getUserWithManagerAndRole(String userUuid);

	List<UserProfileDto> getAllUsers();

	List<UserEntity> getUserByEmail(String email);

	boolean updateUserTypeAndDepartment(UpdateDepartmentUserTypeDto updateDepartmentUserTypeDto);

	UserDto updateUser(UpdateUserRequestDto updateUserRequestDto);

	UserDto updateUserMobile(UpdateUserRequestDto updateUserRequestDto);

	boolean updateUserStatus(String mobileNo, String userType);

	UserDto updateUserType(String mobileNo, String userType);
	UserDto getUserForAccessLevelAndRole(@Valid AccessLevelRoleRequestDto cityRolesRequestDto);

	boolean createRoleBaseUser(UserType userType,String roleUuid,String accessLevelUuid);

}