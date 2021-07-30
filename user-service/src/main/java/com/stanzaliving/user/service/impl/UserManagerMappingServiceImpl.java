package com.stanzaliving.user.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.stanzaliving.core.base.common.dto.PaginationRequest;
import com.stanzaliving.core.base.exception.ApiValidationException;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.enums.UserManagerMappingType;
import com.stanzaliving.core.user.request.dto.UserManagerMappingRequestDto;
import com.stanzaliving.user.entity.UserManagerMappingEntity;
import com.stanzaliving.user.repository.UserManagerMappingRepository;
import com.stanzaliving.user.service.UserManagerMappingService;
import com.stanzaliving.user.service.UserService;

import lombok.extern.log4j.Log4j2;

/**
 * @author raj.kumar
 *
 */
@Service
@Log4j2
public class UserManagerMappingServiceImpl implements UserManagerMappingService {

	@Autowired
	private UserService userService;

	@Autowired
	private UserManagerMappingRepository userManagerMappingRepository;

	@Override
	public void createUserManagerMapping(UserManagerMappingRequestDto userManagerMappingDto) {

		if (!isUserIdAndManagerIdValid(userManagerMappingDto.getUserId(), userManagerMappingDto.getManagerId())) {
			throw new ApiValidationException("Invalid userId or managerId");
		}

		UserManagerMappingEntity mappingEntity = userManagerMappingRepository.findByUserId(userManagerMappingDto.getUserId());

		if (Objects.isNull(mappingEntity)) {

			log.info("Adding new manager mapping for user: {}", userManagerMappingDto.getUserId());

			mappingEntity =
					UserManagerMappingEntity.builder()
							.userId(userManagerMappingDto.getUserId())
							.createdBy(userManagerMappingDto.getChangedBy())
							.build();
		}

		mappingEntity.setManagerId(userManagerMappingDto.getManagerId());
		mappingEntity.setUpdatedBy(userManagerMappingDto.getChangedBy());

		userManagerMappingRepository.save(mappingEntity);
	}

	private boolean isUserIdAndManagerIdValid(String userId, String managerId) {
		return Objects.nonNull(userService.getActiveUserByUserId(userId)) && Objects.nonNull(userService.getActiveUserByUserId(managerId));
	}

	@Override
	public List<String> getUserIdsMappedWithManagerId(String managerId) {

		List<UserManagerMappingEntity> userManagerMappingRecords = userManagerMappingRepository
				.findByManagerIdAndStatus(managerId, true);

		if (CollectionUtils.isEmpty(userManagerMappingRecords)) {
			return Collections.emptyList();
		}

		List<String> userIds = userManagerMappingRecords.stream().map(UserManagerMappingEntity::getUserId)
				.collect(Collectors.toList());

		return userIds;
	}

	@Override
	public String findManagerNameForUser(String userId) {

		UserManagerMappingEntity userManagerMappingEntity = userManagerMappingRepository.findByUserId(userId);

		if (userManagerMappingEntity != null) {

			UserProfileDto userProfileDto = userService
					.getUserProfile(userManagerMappingEntity.getManagerId());

			return (Objects.nonNull(userProfileDto))
					? userProfileDto.getFirstName() + " " + userProfileDto.getLastName()
					: null;
		}

		return null;
	}

	@Override
	public UserProfileDto getManagerProfileForUser(String userId) {

		UserManagerMappingEntity userManagerMappingEntity = userManagerMappingRepository.findByUserId(userId);

		if (userManagerMappingEntity != null) {

			return userService.getUserProfile(userManagerMappingEntity.getManagerId());
		}

		return null;
	}

	@Override
	public Map<String, UserProfileDto> getManagerProfileForUserIn(List<String> userIds) {

		List<UserManagerMappingEntity> userManagerMappingEntities = userManagerMappingRepository.findByUserIdIn(userIds);

		return getUserDetails(userManagerMappingEntities);

	}

	@Override
	public UserProfileDto getUserManagerMappingHierarchy(String userId, UserManagerMappingType mappingType) {

		try {

			return getUserManagerMappingHelper(userId, mappingType, 1);

		} catch (Exception e) {
			log.error(" Exception occurred while fetching user manager mapping ", e);
		}

		return null;
	}

	private UserProfileDto getUserManagerMappingHelper(String userId, UserManagerMappingType mappingType, int count) throws Exception {

		// UserManagerMappingEntity userManagerMappingEntity = userManagerMappingRepository.findByUserId(userId);
		//
		// /*
		// As of now, we have maximum of 3 level
		// City Head, Regional Head, National Head
		// */
		// if (count > 3 || userManagerMappingEntity == null) {
		// throw new Exception(" User manager mapping is not found for manager type " + mappingType);
		// }
		//
		// if (userManagerMappingEntity.getUserManagerMappingType().equals(mappingType)) {
		// return userService.getUserProfile(userManagerMappingEntity.getManagerId());
		// }
		//
		// return getUserManagerMappingHelper(userId, mappingType, count+1);

		return null;

	}

	@Override
	public List<UserProfileDto> getPeopleReportingToManager(String managerId) {

		List<UserManagerMappingEntity> userManagerMappingEntities = userManagerMappingRepository.findByManagerId(managerId);

		if (!CollectionUtils.isEmpty(userManagerMappingEntities)) {

			List<String> userIds = userManagerMappingEntities
					.stream()
					.map(UserManagerMappingEntity::getUserId).collect(Collectors.toList());

			PaginationRequest pagination = PaginationRequest.builder().pageNo(1).limit(100).build();
			UserFilterDto userFilterDto = UserFilterDto.builder().userIds(userIds).pageRequest(pagination).build();
			return userService.searchUser(userFilterDto).getData();
		}

		return Collections.emptyList();

	}

	@Override
	public void deleteManagerMapping(String uuid) {
		UserManagerMappingEntity userManagerMappingEntity = userManagerMappingRepository.findFirstByUuid(uuid);
		if (userManagerMappingEntity == null) {
			throw new ApiValidationException("Manager mapping does not exist for id: " + uuid);
		}
		userManagerMappingRepository.delete(userManagerMappingEntity);
	}

	@Override
	public void deleteManagerMappingForUser(String userUuid) {

		UserManagerMappingEntity userManagerMappingEntity = userManagerMappingRepository.findByUserId(userUuid);

		if (Objects.nonNull(userManagerMappingEntity)) {
			log.info("Deleting user manager mapping entity: {}", userManagerMappingEntity);
			userManagerMappingRepository.delete(userManagerMappingEntity);
		}

	}

	private Map<String, UserProfileDto> getUserDetails(List<UserManagerMappingEntity> userManagerMappingEntities) {
		if (!CollectionUtils.isEmpty(userManagerMappingEntities)) {
			Map<String, String> userManagerUuidMap = new HashMap<>();

			userManagerMappingEntities.forEach(userManagerMapping -> {
				userManagerUuidMap.put(userManagerMapping.getUserId(), userManagerMapping.getManagerId());
			});

			return userService.getUserProfileIn(userManagerUuidMap);
		}
		return Collections.emptyMap();
	}
}