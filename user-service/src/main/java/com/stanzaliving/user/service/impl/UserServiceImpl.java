/**
 * 
 */
package com.stanzaliving.user.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.exception.NoRecordException;
import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.base.utils.PhoneNumberUtils;
import com.stanzaliving.core.kafka.dto.KafkaDTO;
import com.stanzaliving.core.kafka.producer.NotificationProducer;
import com.stanzaliving.core.sqljpa.adapter.AddressAdapter;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.dto.UserManagerAndRoleDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.core.user.request.dto.CreateUserAndRoleDto;
import com.stanzaliving.core.user.request.dto.UpdateDepartmentUserTypeDto;
import com.stanzaliving.core.user.request.dto.UpdateUserRequestDto;
import com.stanzaliving.user.acl.service.AclUserService;
import com.stanzaliving.user.acl.service.RoleService;
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
	private NotificationProducer notificationProducer;

	@Autowired
	private RoleService roleService;

	@Value("${kafka.resident.detail.topic}")
	private String kafkaResidentDetailTopic;

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
			log.error("Number: " + addUserRequestDto.getMobile() + " and ISO: " + addUserRequestDto.getIsoCode()
					+ " doesn't appear to be a valid mobile combination");
			throw new StanzaException("Mobile Number and ISO Code combination not valid");
		}

		UserEntity userEntity = userDbService.getUserForMobile(addUserRequestDto.getMobile(),
				addUserRequestDto.getIsoCode());

		if (Objects.nonNull(userEntity)) {
			log.warn("User: " + userEntity.getUuid() + " already exists for Mobile: " + addUserRequestDto.getMobile()
					+ ", ISO Code: " + addUserRequestDto.getIsoCode() + " of type: " + addUserRequestDto.getUserType());
			throw new StanzaException("User already exists with mobile");
		}

		log.info("Adding new User [Mobile: " + addUserRequestDto.getMobile() + ", ISOCode: "
				+ addUserRequestDto.getIsoCode() + ", UserType: " + addUserRequestDto.getUserType() + "]");

		UserProfileEntity profileEntity = UserAdapter.getUserProfileEntity(addUserRequestDto);

		userEntity = UserEntity.builder().userType(addUserRequestDto.getUserType())
				.isoCode(addUserRequestDto.getIsoCode()).mobile(addUserRequestDto.getMobile()).mobileVerified(false)
				.email(addUserRequestDto.getEmail()).emailVerified(false).userProfile(profileEntity).status(true)
				.department(addUserRequestDto.getDepartment()).build();

		profileEntity.setUser(userEntity);

		userEntity = userDbService.saveAndFlush(userEntity);

		log.info("Added New User with Id: " + userEntity.getUuid());

		UserDto userDto = UserAdapter.getUserDto(userEntity);

		KafkaDTO kafkaDTO = new KafkaDTO();
		kafkaDTO.setData(userDto);

		notificationProducer.publish(kafkaResidentDetailTopic, KafkaDTO.class.getName(), kafkaDTO);

		return userDto;
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

		log.info("Found " + userPage.getNumberOfElements() + " User Records on Page: " + pageNo
				+ " for Search Criteria");

		List<UserProfileDto> userDtos = userPage.getContent().stream().map(UserAdapter::getUserProfileDto)
				.collect(Collectors.toList());

		return new PageResponse<>(pageNo, userPage.getNumberOfElements(), userPage.getTotalPages(),
				userPage.getTotalElements(), userDtos);

	}

	private Page<UserEntity> getUserPage(UserFilterDto userFilterDto) {

		Specification<UserEntity> specification = userDbService.getSearchQuery(userFilterDto);

		Pageable pagination = getPaginationForSearchRequest(userFilterDto.getPageRequest().getPageNo(),
				userFilterDto.getPageRequest().getLimit());

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

		return UserManagerAndRoleDto.builder().userProfile(userProfile).manager(managerProfile).roles(roleDtoList)
				.build();
	}

	@Override
	public List<UserProfileDto> getAllUsers() {

		List<UserEntity> userEntities = userDbService.findAll();

		return UserAdapter.getUserProfileDtos(userEntities);
	}

	@Override
	public List<UserEntity> getUserByEmail(String email) {
		return userDbService.findByEmail(email);
	}

	@Override
	public boolean updateUserTypeAndDepartment(UpdateDepartmentUserTypeDto updateDepartmentUserTypeDto) {

		log.info("Searching User by UserId: " + updateDepartmentUserTypeDto.getUserId());

		UserEntity userEntity = userDbService.findByUuidAndStatus(updateDepartmentUserTypeDto.getUserId(),
				Boolean.TRUE);

		if (Objects.isNull(userEntity))
			throw new StanzaException("User not found for UserId: " + updateDepartmentUserTypeDto.getUserId());

		userEntity.setUserType(updateDepartmentUserTypeDto.getUserType());
		userEntity.setDepartment(updateDepartmentUserTypeDto.getDepartment());

		userEntity = userDbService.update(userEntity);

		if (Objects.nonNull(userEntity))
			return Boolean.TRUE;

		return Boolean.FALSE;
	}

	@Override
	public UserDto updateUser(UpdateUserRequestDto updateUserRequestDto) {

		UserEntity userEntity = userDbService.findByUuidAndStatus(updateUserRequestDto.getUserId(), true);

		if (Objects.isNull(userEntity)) {
			throw new StanzaException("User not found for UserId: " + updateUserRequestDto.getUserId());
		}

		if (Objects.nonNull(updateUserRequestDto.getAddress())) {
			userEntity.getUserProfile().setAddress(AddressAdapter.getAddressEntity(updateUserRequestDto.getAddress()));
		}
		if (Objects.nonNull(updateUserRequestDto.getBirthday())) {
			userEntity.getUserProfile().setBirthday(updateUserRequestDto.getBirthday());
		}
		if (Objects.nonNull(updateUserRequestDto.getBloodGroup())) {
			userEntity.getUserProfile().setBloodGroup(updateUserRequestDto.getBloodGroup());
		}
		if (Objects.nonNull(updateUserRequestDto.getEmail())) {
			userEntity.setEmail(updateUserRequestDto.getEmail());
		}
		if (Objects.nonNull(updateUserRequestDto.getFirstName())) {
			userEntity.getUserProfile().setFirstName(updateUserRequestDto.getFirstName());
		}
		if (Objects.nonNull(updateUserRequestDto.getGender())) {
			userEntity.getUserProfile().setGender(updateUserRequestDto.getGender());
		}
		;
		if (Objects.nonNull(updateUserRequestDto.getLastName())) {
			userEntity.getUserProfile().setLastName(updateUserRequestDto.getLastName());
		}
		if (Objects.nonNull(updateUserRequestDto.getNationality())) {
			userEntity.getUserProfile().setNationality(updateUserRequestDto.getNationality());
		}
		if (Objects.nonNull(updateUserRequestDto.getProfilePicture())) {
			userEntity.getUserProfile().setProfilePicture(updateUserRequestDto.getProfilePicture());
		}
		if (Objects.nonNull(updateUserRequestDto.getDateOfArrival())) {
			userEntity.getUserProfile().setArrivalDate(updateUserRequestDto.getDateOfArrival());
		}
		if (Objects.nonNull(updateUserRequestDto.getForiegnCountryCode())) {
			userEntity.getUserProfile().setSecondaryIsoCode(updateUserRequestDto.getForiegnCountryCode());
		}
		if (Objects.nonNull(updateUserRequestDto.getForiegnMobileNumber())) {
			userEntity.getUserProfile().setProfilePicture(updateUserRequestDto.getForiegnMobileNumber());
		}
		if (Objects.nonNull(updateUserRequestDto.getNextDestination())) {
			userEntity.getUserProfile().setNextDestination(updateUserRequestDto.getNextDestination());
		}
		userEntity = userDbService.update(userEntity);

		UserProfileDto userProfileDto = UserAdapter.getUserProfileDto(userEntity);

		KafkaDTO kafkaDTO = new KafkaDTO();
		kafkaDTO.setData(userProfileDto);

		notificationProducer.publish(kafkaResidentDetailTopic, KafkaDTO.class.getName(), kafkaDTO);

		return userProfileDto;
	}

	@Transactional
	@Override
	public UserDto addUserAndRole(CreateUserAndRoleDto createUserAndRoleDto) {

		UserEntity userEntity = userDbService.getUserForMobile(createUserAndRoleDto.getAddUserRequestDto().getMobile(),
				createUserAndRoleDto.getAddUserRequestDto().getIsoCode());
		UserDto userDto = UserDto.builder().build();
		if (Objects.isNull(userEntity)) {
			userDto = addUser(createUserAndRoleDto.getAddUserRequestDto());
		} else {
			userDto = UserAdapter.getUserDto(userEntity);
		}

		createUserAndRoleDto.getAddUserDeptLevelRoleDto().setUserUuid(userDto.getUuid());

		List<RoleDto> roleDto = roleService.findByDepartmentAndAccessLevel(
				createUserAndRoleDto.getAddUserDeptLevelRoleDto().getDepartment(),
				createUserAndRoleDto.getAddUserDeptLevelRoleDto().getAccessLevel());

		if (CollectionUtils.isEmpty(roleDto)) {
			log.error("Department: " + createUserAndRoleDto.getAddUserDeptLevelRoleDto().getDepartment()
					+ " and Access Level: " + createUserAndRoleDto.getAddUserDeptLevelRoleDto().getAccessLevel()
					+ " not available in role table.");

			throw new StanzaException("Role not exists in Role Table.");
		}
		roleDto.forEach(role -> {
			createUserAndRoleDto.getAddUserDeptLevelRoleDto().setRolesUuid(Arrays.asList(role.getUuid()));
			aclUserService.addRole(createUserAndRoleDto.getAddUserDeptLevelRoleDto());
		});

		return userDto;

	}
}