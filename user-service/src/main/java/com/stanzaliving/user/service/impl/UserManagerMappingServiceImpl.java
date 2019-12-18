/**
 * 
 */
package com.stanzaliving.user.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.request.dto.UserManagerMappingRequestDto;
import com.stanzaliving.user.entity.UserManagerMappingEntity;
import com.stanzaliving.user.repository.UserManagerMappingRepository;
import com.stanzaliving.user.service.UserManagerMappingService;
import com.stanzaliving.user.service.UserService;

/**
 * @author raj.kumar
 *
 */
@Service
public class UserManagerMappingServiceImpl implements UserManagerMappingService {

	@Autowired
	private UserService userService;
	
	@Autowired
	private UserManagerMappingRepository userManagerMappingRepository;
	
	@Override
	public void createUserManagerMapping(UserManagerMappingRequestDto userManagerMappingDto) {
		
		
			if(!isUserIdAndManagerIdValid(userManagerMappingDto.getUserId(), userManagerMappingDto.getManagerId())) {
			
				throw new StanzaException(" Invalid userId or managerId ");
			}
			
			UserManagerMappingEntity mappingEntity = new UserManagerMappingEntity();
			mappingEntity.setManagerId(userManagerMappingDto.getManagerId());
			mappingEntity.setUserId(userManagerMappingDto.getUserId());
			mappingEntity.setCreatedBy(userManagerMappingDto.getChangedBy());
			mappingEntity.setUpdatedBy(userManagerMappingDto.getChangedBy());
			
			userManagerMappingRepository.save(mappingEntity);
	}
	
	private boolean isUserIdAndManagerIdValid(String userId, String managerId) {
	   
		return	(!Objects.isNull(userService.getActiveUserByUserId(userId))
					&& !Objects.isNull(userService.getActiveUserByUserId(managerId)));
		
		
	}

	@Override
	public List<String> getUserIdsMappedWithManagerId(String managerId) {
		
		List<UserManagerMappingEntity> userManagerMappingRecords = 
								userManagerMappingRepository.findByManagerIdAndStatus(managerId, true);
		
		
		if(CollectionUtils.isEmpty(userManagerMappingRecords)) {
			return Collections.emptyList();
		}
		
		List<String> userIds = 
				userManagerMappingRecords.stream().map(UserManagerMappingEntity::getUserId).collect(Collectors.toList());
		
		return userIds;
	}

	@Override
	public String findManagerNameForUser(String userId) {
		
		UserManagerMappingEntity userManagerMappingEntity = userManagerMappingRepository.findByUserId(userId);
		
		if(Objects.nonNull(userManagerMappingEntity)) {
		
			UserProfileDto userProfileDto = userService.getUserProfile(userManagerMappingEntity.getManagerId());
		
			return (Objects.nonNull(userProfileDto))?userProfileDto.getFirstName() + " " + userProfileDto.getLastName():null;
		}
		
		return null;
	}
	
	@Override
	public UserProfileDto getManagerProfileForUser(String userId) {
		
		UserManagerMappingEntity userManagerMappingEntity = userManagerMappingRepository.findByUserId(userId);
		
		if(Objects.nonNull(userManagerMappingEntity)) {
		
			return userService.getUserProfile(userManagerMappingEntity.getManagerId());
		}
		
		return null;
	}

}
