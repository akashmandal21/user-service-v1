/**
 * 
 */
package com.stanzaliving.user.service;

import java.util.List;

import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.request.dto.UserManagerMappingRequestDto;

/**
 * @author raj.kumar
 *
 */
public interface UserManagerMappingService {

	void createUserManagerMapping(UserManagerMappingRequestDto userManagerMappingDto);
	
	List<String> getUserIdsMappedWithManagerId(String managerId);
	
	String findManagerNameForUser(String userId);

	UserProfileDto getManagerProfileForUser(String userId);
}
