package com.stanzaliving.user.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.stanzaliving.core.base.common.dto.PaginationRequest;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelRoleDbService;
import com.stanzaliving.user.adapters.UserAdapter;
import com.stanzaliving.user.adapters.Userv2ToUserAdapter;
import com.stanzaliving.user.dto.userv2.UserAttributesDto;
import com.stanzaliving.user.dto.userv2.UserDto;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.feignclient.UserV2FeignService;
import com.stanzaliving.user.feignclient.Userv2HttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.stanzaliving.core.base.exception.ApiValidationException;
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

	@Autowired
	@Lazy
	private Userv2HttpService userv2HttpService;

	@Autowired
	@Lazy
	private UserV2FeignService userV2FeignService;

	@Autowired
	private UserDepartmentLevelDbService userDepartmentLevelDbService;

	@Autowired
	private UserDepartmentLevelRoleDbService userDepartmentLevelRoleDbService;

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

		List<String> userIds=userV2FeignService.getUserUuidsMappedWithManagerUuid(managerId);

		List<UserManagerMappingEntity> userManagerMappingRecords = userManagerMappingRepository
				.findByManagerIdAndStatus(managerId, true);

		if (!CollectionUtils.isEmpty(userManagerMappingRecords)) {
			userIds.addAll(userManagerMappingRecords.stream().map(UserManagerMappingEntity::getUserId)
					.collect(Collectors.toList()));
		}

		return userIds;
	}

	@Override
	public String findManagerNameForUser(String userId) {


		UserDto users=userV2FeignService.getManagerForUser(userId);
		if(Objects.nonNull(users)){
			return users.getFirstName() + " " + users.getLastName();
		}

		UserManagerMappingEntity userManagerMappingEntity = userManagerMappingRepository.findByUserId(userId);

		if (userManagerMappingEntity != null) {

			UserProfileDto userProfileDto = userService
					.getUserByUserId(userManagerMappingEntity.getManagerId());

			return (Objects.nonNull(userProfileDto))
					? userProfileDto.getFirstName() + " " + userProfileDto.getLastName()
					: null;
		}
		return null;
	}

	@Override
	public UserProfileDto getManagerProfileForUser(String userId) {

		UserDto user=userV2FeignService.getManagerForUser(userId);
		UserEntity userEntity=null;

		if (Objects.nonNull(user)) {
			userEntity=Userv2ToUserAdapter.getUserEntityFromUserv2(user);
			return UserAdapter.getUserProfileDto(userEntity);
		}
		UserManagerMappingEntity userManagerMappingEntity = userManagerMappingRepository.findByUserId(userId);
		if (userManagerMappingEntity != null) {
			return userService.getUserByUserId(userManagerMappingEntity.getManagerId());
		}

		return null;
	}

	@Override
	public Map<String, UserProfileDto> getManagerProfileForUserIn(List<String> userIds) {

		Map<String,UserDto> users=userV2FeignService.getUserManagers(userIds);
		Map<String,UserProfileDto> userManagerProfileMap=new HashMap<>();

		users.forEach((k,v)->{
			userManagerProfileMap.put(k,UserAdapter.getUserProfileDto(Userv2ToUserAdapter.getUserEntityFromUserv2(v)));
		});

		List<UserManagerMappingEntity> userManagerMappingEntities = userManagerMappingRepository.findByUserIdIn(userIds);
		Map<String, UserProfileDto> userProfileDtoMap= getUserDetails(userManagerMappingEntities);

		if(userManagerProfileMap.size()>0){
			userManagerProfileMap.forEach((k,v)->{
				if(!userProfileDtoMap.containsKey(k)){
					userProfileDtoMap.put(k,v);
				}
			});
		}
		return userProfileDtoMap;
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

		List<UserProfileDto> userv2ProfileDtos=new ArrayList<>();
		List<UserProfileDto> userProfileDtos=new ArrayList<>();
		List<UserDto> users=userV2FeignService.getUsersReportingToManager(managerId);

		if(Objects.nonNull(users) && users.size()>0){
			for(UserDto user: users){
				userv2ProfileDtos.add(UserAdapter.getUserProfileDto(Userv2ToUserAdapter.getUserEntityFromUserv2(user)));
			}
		}

		if (!CollectionUtils.isEmpty(userManagerMappingEntities)) {

			List<String> userIds = userManagerMappingEntities
					.stream()
					.map(UserManagerMappingEntity::getUserId).collect(Collectors.toList());

			PaginationRequest pagination = PaginationRequest.builder().pageNo(1).limit(100).build();
			UserFilterDto userFilterDto = UserFilterDto.builder().userIds(userIds).status(true).pageRequest(pagination).build();
			userProfileDtos= userService.searchUser(userFilterDto).getData();
		}

		if(userv2ProfileDtos.size()>0){
			userProfileDtos.addAll(userv2ProfileDtos);
		}
		return userProfileDtos;

	}

	@Override
	public List<UserProfileDto> getPeopleReportingToManagerForZonalHead(String managerId, Department department) {
		List<UserProfileDto> userProfileDtos=getPeopleReportingToManager(managerId);
		List<UserProfileDto> filteredUserProfileDtos=new ArrayList<>();
		List<String> roleNames=null;
		if(Department.DESIGN.equals(department)){
			for(UserProfileDto userProfileDto:userProfileDtos){
				if(UserType.DESIGN_COORDINATOR.equals(userProfileDto.getUserType())){
					filteredUserProfileDtos.add(userProfileDto);
				}
				else{
					roleNames=Arrays.asList("DESIGN_COORDINATOR");
					if(checkIfRolePresent(userProfileDto,roleNames)){
						filteredUserProfileDtos.add(userProfileDto);
					}
				}
			}
		}
		else{
			for(UserProfileDto userProfileDto:userProfileDtos){
				if(UserType.PROJECT_MANAGER.equals(userProfileDto.getUserType())){
					filteredUserProfileDtos.add(userProfileDto);
				}
				else{
					roleNames=Arrays.asList("Project Manager","PROJECTS_MANAGER","DPM");
					if(checkIfRolePresent(userProfileDto,roleNames)){
						filteredUserProfileDtos.add(userProfileDto);
					}
				}
			}
		}
		return filteredUserProfileDtos;
	}

	public boolean checkIfRolePresent(UserProfileDto userProfileDto,List<String> roleNames){
		List<String> listOfRoles=userDepartmentLevelDbService.findListOfRoles(userProfileDto.getUuid());
		if(org.apache.commons.collections.CollectionUtils.isNotEmpty(listOfRoles)){
			for(String roleName:roleNames) {
				if(listOfRoles.contains(roleName)){
					return true;
				}
			}
		}
		if(userProfileDto.isMigrated()){
			return userV2FeignService.checkIfPermissionPresent(userProfileDto.getUuid(),roleNames);
		}
		return false;
	}



	@Override
	public void deleteManagerMapping(String userUuid) {
		UserManagerMappingEntity userManagerMappingEntity = userManagerMappingRepository.findFirstByUserId(userUuid);
		if (userManagerMappingEntity == null) {
			try {
				userv2HttpService.deleteManagerUuidFromUserAttributes(userUuid);
			}
			catch (ApiValidationException e) {
				throw new ApiValidationException("Manager mapping does not exist for id: " + userUuid);
			}
		}
		userManagerMappingRepository.delete(userManagerMappingEntity);
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