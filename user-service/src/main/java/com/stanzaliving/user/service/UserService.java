/**
 * 
 */
package com.stanzaliving.user.service;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;

import java.util.List;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface UserService {

	UserDto getActiveUserByUserId(String userId);

	void assertActiveUserByUserUuid(String userId);

	UserDto addUser(AddUserRequestDto addUserRequestDto);

	UserProfileDto getUserProfile(String userId);

	/**
	 * Search users based on the available parameters. All parameters has to be exact match to search. All parameters are optional except pageNo and limit
	 * 
	 * @param userIds
	 * @param mobile
	 * @param isoCode
	 * @param email
	 * @param userType
	 * @param status
	 * @param pageNo
	 * @param limit
	 * 
	 * @return paged response of users matching search criteria
	 */
	PageResponse<UserProfileDto> searchUser(List<String> userIds, String mobile, String isoCode, String email, UserType userType, Boolean status, int pageNo, int limit);

}