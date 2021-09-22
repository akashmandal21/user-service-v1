/**
 * 
 */
package com.stanzaliving.user.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.Valid;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.common.dto.PaginationRequest;
import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.base.exception.ApiValidationException;
import com.stanzaliving.core.base.exception.NoRecordException;
import com.stanzaliving.core.base.utils.PhoneNumberUtils;
import com.stanzaliving.core.generic.dto.UIKeyValue;
import com.stanzaliving.core.kafka.dto.KafkaDTO;
import com.stanzaliving.core.kafka.producer.NotificationProducer;
import com.stanzaliving.core.sqljpa.adapter.AddressAdapter;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleRequestDto;
import com.stanzaliving.core.user.dto.AccessLevelRoleRequestDto;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.dto.UserManagerAndRoleDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.dto.UserRoleCacheDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.core.user.request.dto.ActiveUserRequestDto;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.core.user.request.dto.UpdateDepartmentUserTypeDto;
import com.stanzaliving.core.user.request.dto.UpdateUserRequestDto;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelRoleDbService;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import com.stanzaliving.user.acl.service.AclUserService;
import com.stanzaliving.user.acl.service.RoleService;
import com.stanzaliving.user.acl.service.UserDepartmentLevelRoleService;
import com.stanzaliving.user.acl.service.UserDepartmentLevelService;
import com.stanzaliving.user.adapters.UserAdapter;
import com.stanzaliving.user.constants.UserConstants;
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
	private UserDepartmentLevelRoleDbService userDepartmentLevelRoleDbService;

	@Autowired
	private UserDepartmentLevelDbService userDepartmentLevelDbService;

	@Autowired
	private UserManagerMappingService userManagerMappingService;

	@Autowired
	private AclUserService aclUserService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private UserDepartmentLevelRoleService userDepartmentLevelRoleService;

	@Autowired
	private UserDepartmentLevelService userDepartmentLevelService;

	@Autowired
	private NotificationProducer notificationProducer;

	@PersistenceContext
	private EntityManager entityManager;

	@Value("${kafka.resident.detail.topic}")
	private String kafkaResidentDetailTopic;

	@Value("${consumer.role}")
	private String consumerUuid;

	@Value("${broker.role}")
	private String brokerUuid;
	
	@Value("${country.uuid}")
	private String countryUuid;
	
	@Override
	public UserProfileDto getActiveUserByUserId(String userId) {

		log.info("Searching User by UserId: " + userId);

		UserEntity userEntity = userDbService.findByUuidAndStatus(userId, true);

		if (Objects.isNull(userEntity)) {
			throw new ApiValidationException("User not found for UserId: " + userId);
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
			throw new ApiValidationException("Mobile Number and ISO Code combination not valid");
		}

		UserEntity userEntity = userDbService.getUserForMobile(addUserRequestDto.getMobile(),
				addUserRequestDto.getIsoCode());

		if (Objects.nonNull(userEntity)) {

			if(!userEntity.isStatus()){
				userEntity.setStatus(true);
				userDbService.update(userEntity);
			}

			log.warn("User: " + userEntity.getUuid() + " already exists for Mobile: " + addUserRequestDto.getMobile()
					+ ", ISO Code: " + addUserRequestDto.getIsoCode() + " of type: " + addUserRequestDto.getUserType());
			
			if(addUserRequestDto.getUserType().equals(UserType.CONSUMER)|| addUserRequestDto.getUserType().equals(UserType.EXTERNAL)) {
				userEntity.setUserType(addUserRequestDto.getUserType());
				try {
					addUserOrConsumerRole(userEntity);
				}catch (Exception e) {
					log.error("Got error while adding role",e);
				}
			}
			
			
			return UserAdapter.getUserDto(userEntity);

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

		
		addUserOrConsumerRole(userEntity);
			

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
			throw new ApiValidationException("User not found for UserId: " + userId);
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
			throw new ApiValidationException("User not found for Uuids: " + managerUuids);
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
			throw new ApiValidationException("User either does not exist or user is already in desired state.");
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
	public List<UserProfileDto> getAllActiveUsersByUuidIn(ActiveUserRequestDto activeUserRequestDto) {
		
		List<UserEntity> userEntities = userDbService.findByUuidInAndStatus(activeUserRequestDto.getUserIds(), true);

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

		if (Objects.isNull(userEntity)) {
			throw new ApiValidationException("User not found for UserId: " + updateDepartmentUserTypeDto.getUserId());
		}

		userEntity.setUserType(updateDepartmentUserTypeDto.getUserType());
		userEntity.setDepartment(updateDepartmentUserTypeDto.getDepartment());

		userEntity = userDbService.update(userEntity);

		addUserOrConsumerRole(userEntity);
		
		return Objects.nonNull(userEntity);
	}

	@Override
	public UserDto updateUser(UpdateUserRequestDto updateUserRequestDto) {

		UserEntity userEntity = userDbService.findByUuid(updateUserRequestDto.getUserId());

		if (Objects.isNull(userEntity)) {
			throw new ApiValidationException("User not found for UserId: " + updateUserRequestDto.getUserId());
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
		if (Objects.nonNull(updateUserRequestDto.getUserMobile())) {
			//not allowing reuse of even inactive user's number.
			//not checking ISO code
			if ((!updateUserRequestDto.getUserMobile().equals(userEntity.getMobile())) && Objects.nonNull(userDbService.findByMobile(updateUserRequestDto.getUserMobile()))) {
				throw new ApiValidationException("User exists for Mobile Number: " + updateUserRequestDto.getUserMobile());
			}
			userEntity.setMobile(updateUserRequestDto.getUserMobile());
		}
		if (Objects.nonNull(updateUserRequestDto.getDepartment())) {
			userEntity.setDepartment(updateUserRequestDto.getDepartment());
		}
		if (Objects.nonNull(updateUserRequestDto.getUserType())) {
			userEntity.setUserType(updateUserRequestDto.getUserType());
		}
		if (Objects.nonNull(updateUserRequestDto.getMiddleName())) {
			userEntity.getUserProfile().setMiddleName(updateUserRequestDto.getMiddleName());
		}
		userEntity = userDbService.update(userEntity);

		UserProfileDto userProfileDto = UserAdapter.getUserProfileDto(userEntity);

		addUserOrConsumerRole(userEntity);
		
		KafkaDTO kafkaDTO = new KafkaDTO();
		kafkaDTO.setData(userProfileDto);

		notificationProducer.publish(kafkaResidentDetailTopic, KafkaDTO.class.getName(), kafkaDTO);

		return userProfileDto;
	}

	private void addUserOrConsumerRole(UserEntity userEntity) {
		if(userEntity.getUserType().equals(UserType.CONSUMER) || userEntity.getUserType().equals(UserType.EXTERNAL)) {
			AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleRequestDto = getRoleDetails(userEntity);
			
			aclUserService.addRole(addUserDeptLevelRoleRequestDto);
		}
	}

	@Override
	public UserDto updateUserMobile(UpdateUserRequestDto updateUserRequestDto) {
		return updateUser(updateUserRequestDto);
	}

	@Override
	public boolean updateUserStatus(String mobileNo, UserType userType, Boolean enabled) {

		UserEntity user = userDbService.findByMobileAndUserType(mobileNo, userType);

		if (user == null) {
			throw new ApiValidationException("User either does not exist.");
		}
		UserProfileEntity userProfile = user.getUserProfile();

		if (userProfile != null) {
			userProfile.setStatus(enabled);
			user.setUserProfile(userProfile);
		}

		user.setStatus(enabled);
		userDbService.save(user);
		return Boolean.TRUE;
	}

	@Override
	public UserDto updateUserType(String mobileNo, String isoCode, UserType userType) {

		UserEntity userEntity = userDbService.getUserForMobile(mobileNo, isoCode);

		if (Objects.isNull(userEntity)) {
			throw new ApiValidationException(
					"User does not exists for Mobile Number: " + mobileNo + " and isoCode :" + isoCode);
		}

		if (Objects.nonNull(userType)) {
			userEntity.setUserType(userType);
		}

		UserDto userDto = UserAdapter.getUserProfileDto(userDbService.update(userEntity));
		
		addUserOrConsumerRole(userEntity);

		return userDto;
	}

	public UserDto getUserForAccessLevelAndRole(@Valid AccessLevelRoleRequestDto cityRolesRequestDto) {
		RoleDto roleDto = roleService.findByRoleNameAndDepartment(cityRolesRequestDto.getRoleName(),
				cityRolesRequestDto.getDepartment());
		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = userDepartmentLevelRoleService
				.findByRoleUuid(roleDto.getUuid());

		if (CollectionUtils.isEmpty(userDepartmentLevelRoleEntityList)) {
			return null;
		}

		for (UserDepartmentLevelRoleEntity userDepartmentLevelRoleEntity : userDepartmentLevelRoleEntityList) {
			UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelService
					.findByUuid(userDepartmentLevelRoleEntity.getUserDepartmentLevelUuid());
			String csvStringOfUuids = userDepartmentLevelEntity.getCsvAccessLevelEntityUuid();

			if (StringUtils.isNotEmpty(csvStringOfUuids)) {
				List<String> accessLevelEntityUuids = Arrays
						.asList(csvStringOfUuids.split(UserConstants.DELIMITER_KEY));
				if (accessLevelEntityUuids.contains(cityRolesRequestDto.getAccessLevelUuid())) {
					UserEntity userEntity = userDbService.findByUuid(userDepartmentLevelEntity.getUserUuid());
					return UserAdapter.getUserDto(userEntity);
				}
			}

		}
		return null;
	}

	@Override
	public boolean createRoleBaseUser(UserType userType) {

		List<UserEntity> userEntity = userDbService.findByUserType(userType);
		
		if (CollectionUtils.isEmpty(userEntity)) {
			log.error("user Type: " + userType + " not available in User table.");
			throw new ApiValidationException("User Type not exists in user Table.");
		}
		userEntity.forEach(user -> {
			if(user.isStatus()) {
				AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleRequestDto = getRoleDetails(user);
				
				aclUserService.addRole(addUserDeptLevelRoleRequestDto);
			}

		});

		return Boolean.TRUE;
	}

	@Override
	public boolean createRoleBaseUser(List<String> mobiles) {
		
		for (String mobile : mobiles) {
			UserEntity userEntity = userDbService.findByMobile(mobile);
			if(Objects.nonNull(userEntity) && userEntity.isStatus()) {
				AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleRequestDto = getRoleDetails(userEntity);
				
				aclUserService.addRole(addUserDeptLevelRoleRequestDto);
			}
		}
		return Boolean.TRUE;
	}

	
	
	private AddUserDeptLevelRoleRequestDto getRoleDetails(UserEntity user) {
		AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleRequestDto = AddUserDeptLevelRoleRequestDto.builder()
				.build();

		addUserDeptLevelRoleRequestDto.setUserUuid(user.getUuid());
		addUserDeptLevelRoleRequestDto.setAccessLevelEntityListUuid(Arrays.asList(countryUuid));

		if (user.getUserType().getTypeName().equalsIgnoreCase("Consumer")) {
			addUserDeptLevelRoleRequestDto.setRolesUuid(Arrays.asList(consumerUuid));
			addUserDeptLevelRoleRequestDto.setAccessLevel(AccessLevel.valueOf("COUNTRY"));
			addUserDeptLevelRoleRequestDto.setDepartment(user.getDepartment());
		} else if (user.getUserType().getTypeName().equalsIgnoreCase("External")) {
			addUserDeptLevelRoleRequestDto.setRolesUuid(Arrays.asList(brokerUuid));
			addUserDeptLevelRoleRequestDto.setAccessLevel(AccessLevel.valueOf("COUNTRY"));
			addUserDeptLevelRoleRequestDto.setDepartment(user.getDepartment());
		}

		return addUserDeptLevelRoleRequestDto;
	}

	@Override
	public Map<String, UserProfileDto> getUserProfileDto(Set<String> mobileNos) {
		
		Map<String, UserProfileDto> userMap = new HashMap<>();
		
		List<UserProfileDto> userProfileDto=UserAdapter.getUserProfileDtos(userDbService.findByMobileIn(mobileNos));

		userProfileDto.forEach(user -> {
			userMap.put(user.getMobile(), user);
		});
		
		return userMap;
	}

	@Override
	public UserProfileDto getUserDetails(String mobileNo) {

			log.info("Searching User by UserId: " + mobileNo);

			UserEntity userEntity = userDbService.findByMobile(mobileNo);

			if (Objects.isNull(userEntity)) {
				throw new ApiValidationException("User not found for mobileNo: " + mobileNo);
			}

			return UserAdapter.getUserProfileDto(userEntity);
		}


		@Override
		public Map<String, UserProfileDto> getUserProfileForUserIn(List<String> userUuids) {
			
			Map<String, UserProfileDto> userMap = new HashMap<>();

			List<UserProfileDto> userProfileDto = UserAdapter.getUserProfileDtos(userDbService.findByUuidIn(userUuids));

			userProfileDto.forEach(user -> {
				userMap.put(user.getUuid(), user);
			});

			return userMap;
		}

	@Override
	public List<UserRoleCacheDto> getCacheableForRoles(List<String> roleNames) {

		log.info("Got request to get list of userid by rolenames {}", roleNames);
		Map<String,UserRoleCacheDto> cacheDtos = new HashMap<>(Department.values().length*roleNames.size());
		Set<String> users = new HashSet<>();
		for(Department department: Department.values()) {

//			log.info("Department {}",department);

			List<RoleDto> roleDtos = roleService.findByRoleNameInAndDepartment(roleNames, department);

//			log.info("Roles {}",roleDtos);


			for (RoleDto roleDto : roleDtos) {
				if (Objects.nonNull(roleDtos) && roleDto.getDepartment().equals(department)) {

					List<UserDepartmentLevelRoleEntity> departmentLevelRoleEntities = userDepartmentLevelRoleDbService.findByRoleUuid(roleDto.getUuid());

//					log.info("Department Role Entity {}",departmentLevelRoleEntities);

					if (CollectionUtils.isNotEmpty(departmentLevelRoleEntities)) {

						List<String> uuids = departmentLevelRoleEntities.stream().map(UserDepartmentLevelRoleEntity::getUserDepartmentLevelUuid).collect(Collectors.toList());

						List<UserDepartmentLevelEntity> departmentLevelEntities = userDepartmentLevelDbService.findByUuidInAndAccessLevel(uuids, roleDto.getAccessLevel());

//						log.info("Department Level Entity {}",departmentLevelEntities);

						if (CollectionUtils.isNotEmpty(departmentLevelEntities)) {
							String key = roleDto.getRoleName()+""+department;
							cacheDtos.putIfAbsent(key,UserRoleCacheDto.builder().roleName(roleDto.getRoleName()).department(department).accessUserMap(new HashMap<>()).build());
							departmentLevelEntities.forEach(entity -> {
								Arrays.asList((entity.getCsvAccessLevelEntityUuid().split(","))).stream().forEach(accessUuid -> {
									cacheDtos.get(key).getAccessUserMap().putIfAbsent(accessUuid, new ArrayList<>());
									cacheDtos.get(key).getAccessUserMap().get(accessUuid).add(new UIKeyValue(entity.getUserUuid(), entity.getUserUuid()));
									users.add(entity.getUserUuid());
								});
							});
						}
					}
				}
			}

		}

		if(CollectionUtils.isNotEmpty(users)){
			PaginationRequest paginationRequest = PaginationRequest.builder().pageNo(1).limit(users.size()).build();
			Map<String,String> userNames = this.searchUser(UserFilterDto.builder().pageRequest(paginationRequest).userIds(users.stream().collect(Collectors.toList())).build()).getData()
					.stream().collect(Collectors.toMap(f->f.getUuid(), f->getUserName(f)));
			return cacheDtos.values().stream().map(userRoleCacheDto -> {
				for (Map.Entry<String, List<UIKeyValue>> entry : userRoleCacheDto.getAccessUserMap().entrySet()) {
					entry.setValue(entry.getValue().stream().map(uiKeyValue -> new UIKeyValue(userNames.getOrDefault(uiKeyValue.getValue(),""),uiKeyValue.getValue())).collect(Collectors.toList()));
				}
				return userRoleCacheDto;
			}).collect(Collectors.toList());


		}
		return ListUtils.EMPTY_LIST;
	}

	private String getUserName(UserProfileDto userProfile){
		StringBuilder name = new StringBuilder();

		if (Objects.nonNull(userProfile)) {
			name.append(StringUtils.defaultString(userProfile.getFirstName())).append( " ");
			name.append(StringUtils.defaultString(userProfile.getMiddleName())).append( " ");
			name.append(StringUtils.defaultString(userProfile.getLastName()));
		}
		return name.toString();
	}

	@Override
	public UserProfileDto getUserProfileDtoByEmail(String email) {

		log.info("Searching User by email: " + email);

		UserEntity userEntity = userDbService.findTop1ByEmailOrderByCreatedAtDesc(email);

		if (Objects.isNull(userEntity)) {
			throw new ApiValidationException("User not found for email: " + email);
		}

		return UserAdapter.getUserProfileDto(userEntity);
	}

	@Override
	public Map<String, String> getUserUuidEmailMap() {

		Query query = entityManager.createNativeQuery("SELECT uuid, email from user");
		List<Object[]> data =  query.getResultList();

		if(Objects.nonNull(data) && CollectionUtils.isNotEmpty(data)){

			Map<String, String> responseMap = new HashMap<>();
			for(Object[] obj: data){

				if(Objects.nonNull(obj))
					responseMap.put((String) obj[0], (String) obj[1]);
			}
			return responseMap;
		}

		return MapUtils.EMPTY_MAP;
	}
}