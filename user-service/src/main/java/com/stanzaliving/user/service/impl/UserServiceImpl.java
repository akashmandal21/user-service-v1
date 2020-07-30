/**
 * 
 */
package com.stanzaliving.user.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.stanzaliving.core.property.manager.PropertyManager;
import com.stanzaliving.core.user.dto.*;
import com.stanzaliving.user.kafka.service.KafkaUserService;
import com.stanzaliving.user.service.GSuiteUserSyncService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.exception.NoRecordException;
import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.base.utils.PhoneNumberUtils;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.user.acl.service.AclUserService;
import com.stanzaliving.user.adapters.UserAdapter;
import com.stanzaliving.user.db.service.UserDbService;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.entity.UserProfileEntity;
import com.stanzaliving.user.service.UserManagerMappingService;
import com.stanzaliving.user.service.UserService;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Log4j2
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDbService userDbService;

	@Autowired
	private UserManagerMappingService userManagerMappingService;

	@Autowired
	private AclUserService aclUserService;

	@Autowired
	private GSuiteUserSyncService gSuiteUserSyncService;

	@Autowired
	private KafkaUserService kafkaUserService;

	@Autowired
	private PropertyManager propertyManager;

	@Override
	public UserProfileDto getActiveUserByUserId(String userId) {

		log.info("Searching User by UserId: " + userId);

		UserEntity userEntity = userDbService.findByUuidAndStatus(userId, true);

		if (Objects.isNull(userEntity)) {
			throw new StanzaException("User not found for UserId: " + userId);
		}

		return UserAdapter.getUserProfileDto(userEntity);
	}

	@Override
	public void assertActiveUserByUserUuid(String userUuid) {
		this.getActiveUserByUserId(userUuid);
	}

	@Override
	public UserDto addUser(AddUserRequestDto addUserRequestDto) {

		if (!PhoneNumberUtils.isValidMobileForCountry(addUserRequestDto.getMobile(), addUserRequestDto.getIsoCode())) {
			log.error("Number: " + addUserRequestDto.getMobile() + " and ISO: " + addUserRequestDto.getIsoCode() + " doesn't appear to be a valid mobile combination");
			throw new StanzaException("Mobile Number and ISO Code combination not valid");
		}

		UserEntity userEntity =
				userDbService.getUserForMobile(addUserRequestDto.getMobile(), addUserRequestDto.getIsoCode());

		if (Objects.nonNull(userEntity)) {
			log.warn("User: " + userEntity.getUuid() + " already exists for Mobile: " + addUserRequestDto.getMobile() + ", ISO Code: " + addUserRequestDto.getIsoCode() + " of type: "
					+ addUserRequestDto.getUserType());
			throw new StanzaException("User already exists with mobile");
		}

		log.info("Adding new User [Mobile: " + addUserRequestDto.getMobile() + ", ISOCode: " + addUserRequestDto.getIsoCode() + ", UserType: " + addUserRequestDto.getUserType() + "]");

		UserProfileEntity profileEntity = UserAdapter.getUserProfileEntity(addUserRequestDto);

		userEntity =
				UserEntity.builder()
						.userType(addUserRequestDto.getUserType())
						.isoCode(addUserRequestDto.getIsoCode())
						.mobile(addUserRequestDto.getMobile())
						.mobileVerified(false)
						.email(addUserRequestDto.getEmail())
						.emailVerified(false)
						.userProfile(profileEntity)
						.status(true)
						.department(addUserRequestDto.getDepartment())
						.build();

		profileEntity.setUser(userEntity);

		userEntity = userDbService.saveAndFlush(userEntity);

		log.info("Added New User with Id: " + userEntity.getUuid());

		return UserAdapter.getUserDto(userEntity);
	}

	@Override
	public UserProfileDto getUserProfile(String userId) {

		UserEntity userEntity = userDbService.findByUuid(userId);

		if (Objects.isNull(userEntity)) {
			throw new StanzaException("User not found for UserId: " + userId);
		}

		return UserAdapter.getUserProfileDto(userEntity);
	}

	@Override
	public Map<String, UserProfileDto> getUserProfileIn(Map<String, String> userManagerUuidMap) {

		Map<String, UserProfileDto> managerProfileDtoMap = new HashMap<>();

		List<String> managerUuids = new ArrayList<>();

		// Extract managerIds
		userManagerUuidMap.forEach((k, v) -> {
			managerUuids.add(v);
		});

		List<UserEntity> userEntities = userDbService.findByUuidIn(managerUuids);

		if (Objects.isNull(userEntities)) {
			throw new StanzaException("User not found for Uuids: " + managerUuids);
		}

		userEntities.forEach(userEntity -> {
			managerProfileDtoMap.put(userEntity.getUuid(), UserAdapter.getUserProfileDto(userEntity));
		});

		Map<String, UserProfileDto> userManagerProfileMapping = new HashMap<>();

		userManagerUuidMap.forEach((k, v) -> {
			userManagerProfileMapping.put(k, managerProfileDtoMap.get(v));
		});

		return userManagerProfileMapping;
	}

	@Override
	public PageResponse<UserProfileDto> searchUser(UserFilterDto userFilterDto) {

		Page<UserEntity> userPage = getUserPage(userFilterDto);

		Integer pageNo = userFilterDto.getPageRequest().getPageNo();

		log.info("Found " + userPage.getNumberOfElements() + " User Records on Page: " + pageNo + " for Search Criteria");

		List<UserProfileDto> userDtos = userPage.getContent().stream().map(UserAdapter::getUserProfileDto).collect(Collectors.toList());

		return new PageResponse<>(pageNo, userPage.getNumberOfElements(), userPage.getTotalPages(), userPage.getTotalElements(), userDtos);

	}

	private Page<UserEntity> getUserPage(UserFilterDto userFilterDto) {

		Specification<UserEntity> specification = userDbService.getSearchQuery(userFilterDto);

		Pageable pagination = getPaginationForSearchRequest(userFilterDto.getPageRequest().getPageNo(), userFilterDto.getPageRequest().getLimit());

		return userDbService.findAll(specification, pagination);
	}

	private Pageable getPaginationForSearchRequest(int pageNo, int limit) {

		Pageable pagination = PageRequest.of(0, 10, Direction.DESC, "createdAt");

		if (pageNo > 0 && limit > 0 && limit < 1000) {
			pagination = PageRequest.of(pageNo - 1, limit, Direction.DESC, "createdAt");
		}

		return pagination;
	}

	@Override
	public boolean updateUserStatus(String userId, Boolean status) {
		UserEntity user = userDbService.findByUuidAndStatus(userId, !status);
		if (user == null) {
			throw new StanzaException("User either does not exist or user is already in desired state.");
		}
		UserProfileEntity userProfile = user.getUserProfile();

		if (userProfile != null) {
			userProfile.setStatus(status);
			user.setUserProfile(userProfile);
		}

		user.setStatus(status);
		userDbService.save(user);
		return true;
	}

	@Override
	public UserManagerAndRoleDto getUserWithManagerAndRole(String userUuid) {
		UserProfileDto userProfile = getUserProfile(userUuid);
		if (userProfile == null) {
			throw new NoRecordException("Please provide valid userId.");
		}
		UserProfileDto managerProfile = userManagerMappingService.getManagerProfileForUser(userUuid);
		List<RoleDto> roleDtoList = aclUserService.getUserRoles(userUuid);

		return UserManagerAndRoleDto.builder()
				.userProfile(userProfile)
				.manager(managerProfile)
				.roles(roleDtoList)
				.build();
	}

	@Override
	public List<UserProfileDto> getAllUsers() {

		List<UserEntity> userEntities = userDbService.findAll();

		return UserAdapter.getUserProfileDtos(userEntities);
	}


	@Override
	public void syncUsersFromGoogle() {
		UserListAndStatusDto userListAndStatusDto = gSuiteUserSyncService.getSegregatedUsers();

		Set<String> inActiveUsersOnGSuite = userListAndStatusDto.getInActivesUsers();

		skipUsers(inActiveUsersOnGSuite);

		markUsersInActive(inActiveUsersOnGSuite);

		kafkaUserService.sendUsersListToKafka(userListAndStatusDto);
	}

	private void skipUsers(Set<String> inActiveUsersOnGsuite) {
		String skipList = propertyManager.getProperty("users.sync.skip.list");

		if (StringUtils.isNotBlank(skipList)) {
			Set<String> skipUsers = new HashSet<>(Arrays.asList(skipList.split(",")));

			inActiveUsersOnGsuite.removeAll(skipUsers);
		}
	}

	private void markUsersInActive(Collection<String> inActivesUsers) {
		List<UserEntity> activeUsers = userDbService.findByEmailInAndStatus(inActivesUsers, true);

		if (CollectionUtils.isNotEmpty(activeUsers)) {
			activeUsers.forEach(user -> user.setStatus(false));

			userDbService.save(activeUsers);
		}
	}


}