/**
 * 
 */
package com.stanzaliving.user.service;

import java.util.Map;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.dto.UserManagerAndRoleDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;

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

}