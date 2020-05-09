/**
 * 
 */
package com.stanzaliving.user.service.impl;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.common.dto.PaginationRequest;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.base.exception.NoRecordException;
import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.base.utils.PhoneNumberUtils;
import com.stanzaliving.core.leadership.dto.UserFilter;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleDto;
import com.stanzaliving.core.user.acl.request.dto.RoleSearchDto;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.dto.UserManagerAndRoleDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.dto.response.UserContactDetailsResponseDto;
import com.stanzaliving.core.user.enums.EnumListing;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.user.acl.db.service.RoleDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelRoleDbService;
import com.stanzaliving.user.acl.entity.RoleEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import com.stanzaliving.user.acl.service.AclUserService;
import com.stanzaliving.user.adapters.UserAdapter;
import com.stanzaliving.user.db.service.UserDbService;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.entity.UserProfileEntity;
import com.stanzaliving.user.service.UserManagerMappingService;
import com.stanzaliving.user.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
	private RoleDbService roleDbService;

	@Autowired
	private UserDepartmentLevelDbService userDepartmentLevelDbService;

	@Autowired
	private UserDepartmentLevelRoleDbService userDepartmentLevelRoleDbService;


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
		
		//Extract managerIds
		userManagerUuidMap.forEach((k,v) -> {
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

	@Override
	public List<UserContactDetailsResponseDto> filterByRoleParams(List<String> roleNames, Department department, int pageNo, int limit) {
		RoleSearchDto roleSearchDto = RoleSearchDto.builder().roleNames(roleNames).department(department).build();
		PaginationRequest paginationRequest = PaginationRequest.builder()
				.pageNo(pageNo).limit(limit).build();
		return filterByRoleParams(roleSearchDto, paginationRequest);
	}


	@Override
	public List<UserContactDetailsResponseDto> filterByRoleParams(RoleSearchDto roleSearchDto, PaginationRequest paginationRequest) {
		List<RoleEntity> roleEntities = roleDbService.findByRoleNameAndDepartment(roleSearchDto.getRoleNames(), roleSearchDto.getDepartment());
		Set<String> userUuids = new HashSet<>();

		roleEntities.forEach(roleEntity -> {
			String roleId = roleEntity.getUuid();
			List<UserDepartmentLevelRoleEntity> userRoleEntities = userDepartmentLevelRoleDbService.findByRoleUuid(roleId);
			Set<String> userDepartmentLevelUuids = userRoleEntities.parallelStream().map(UserDepartmentLevelRoleEntity::getUserDepartmentLevelUuid).collect(Collectors.toSet());
			List<UserDepartmentLevelEntity> userDepartmentLevelEntities = userDepartmentLevelDbService.findByUuidInAndStatus(userDepartmentLevelUuids, true);
			userUuids.addAll(userDepartmentLevelEntities.parallelStream().map(UserDepartmentLevelEntity::getUserUuid).collect(Collectors.toSet()));
		});

		Page<UserEntity> userEntities = userDbService.findByUuids(new ArrayList<>(userUuids), paginationRequest.getPageNo(), paginationRequest.getLimit());
		if (userEntities == null || CollectionUtils.isEmpty(userEntities.getContent())) {
			return Collections.emptyList();
		}
		return userEntities.getContent().parallelStream().map(UserAdapter::convertToContactResponseDto).collect(Collectors.toList());
	}


	private Page<UserEntity> getUserPage(UserFilterDto userFilterDto) {

		Specification<UserEntity> specification = userDbService.getSearchQuery(userFilterDto);

		Pageable pagination = getPaginationForSearchRequest(userFilterDto.getPageRequest().getPageNo(), userFilterDto.getPageRequest().getLimit());

		return userDbService.findAll(specification, pagination);
	}



	private Pageable getPaginationForSearchRequest(int pageNo, int limit) {
		pageNo = Math.max(0, pageNo - 1);
		limit = Math.max(1, limit);
		limit = Math.min(limit, 1000);
		return PageRequest.of(pageNo, limit, Direction.DESC, "createdAt");
	}

	@Override
	public boolean updateUserStatus(String userId, Boolean status) {
		UserEntity user = userDbService.findByUuidAndStatus(userId, !status);
		if(user == null){
			throw new StanzaException("User either does not exist or user is already in desired state.");
		}
		UserProfileEntity userProfile = user.getUserProfile();

		if(userProfile != null){
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
		if(userProfile == null){
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
	public List<EnumListing> getAllUserType() {
		return UserAdapter.getUserTypeEnumAsListing();
	}
}