package com.stanzaliving.user.acl.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.stanzaliving.core.sqljpa.entity.AbstractJpaEntity;
import com.stanzaliving.user.service.UserManagerMappingService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.base.exception.ApiValidationException;
import com.stanzaliving.core.kafka.producer.NotificationProducer;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleListDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleNameUrlExpandedDto;
import com.stanzaliving.core.user.acl.dto.UserRoleSnapshot;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleRequestDto;
import com.stanzaliving.core.user.dto.response.UserContactDetailsResponseDto;
import com.stanzaliving.user.acl.adapters.RoleAdapter;
import com.stanzaliving.user.acl.adapters.UserDepartmentLevelRoleAdapter;
import com.stanzaliving.user.acl.db.service.RoleDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelRoleDbService;
import com.stanzaliving.user.acl.entity.RoleEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import com.stanzaliving.user.acl.service.AclService;
import com.stanzaliving.user.acl.service.AclUserService;
import com.stanzaliving.user.acl.service.RoleService;
import com.stanzaliving.user.acl.service.UserDepartmentLevelRoleService;
import com.stanzaliving.user.acl.service.UserDepartmentLevelService;
import com.stanzaliving.user.adapters.UserAdapter;
import com.stanzaliving.user.db.service.UserDbService;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.service.UserService;

import lombok.extern.log4j.Log4j2;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
public class AclUserServiceImpl implements AclUserService {

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private RoleDbService roleDbService;

	@Autowired
	private UserDbService userDbService;

	@Autowired
	private UserDepartmentLevelService userDepartmentLevelService;

	@Autowired
	private UserManagerMappingService userManagerMappingService;

	@Autowired
	private UserDepartmentLevelDbService userDepartmentLevelDbService;

	@Autowired
	private UserDepartmentLevelRoleService userDepartmentLevelRoleService;

	@Autowired
	private UserDepartmentLevelRoleDbService userDepartmentLevelRoleDbService;

	@Autowired
	private AclService aclService;

	@Autowired
	private NotificationProducer notificationProducer;

	@Value("${kafka.topic.role}")
	private String roleTopic;

	@Override
	public void addRole(AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleDto) {

		userService.assertActiveUserByUserUuid(addUserDeptLevelRoleDto.getUserUuid());

		AddUserDeptLevelRequestDto addUserDeptLevelRequestDto = new AddUserDeptLevelRequestDto(addUserDeptLevelRoleDto);

		UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelService.add(addUserDeptLevelRequestDto);

		userDepartmentLevelRoleService.addRoles(userDepartmentLevelEntity.getUuid(), addUserDeptLevelRoleDto.getRolesUuid());
		publishCurrentRoleSnapshot(addUserDeptLevelRoleDto.getUserUuid());
	}

	@Override
	public void revokeAllRolesOfDepartment(String userUuid, Department department) {

		userService.assertActiveUserByUserUuid(userUuid);

		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuidAndDepartment(userUuid, department);
		if (CollectionUtils.isEmpty(userDepartmentLevelEntityList)) {
			throw new ApiValidationException("User doesn't have any access in this department");
		}

		for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
			userDepartmentLevelService.delete(userDepartmentLevelEntity);
		}
		publishCurrentRoleSnapshot(userUuid);
		return;

	}

	@Override
	public List<UserDeptLevelRoleDto> getActiveUserDeptLevelRole(String userUuid) {
		userService.assertActiveUserByUserUuid(userUuid);
		return getUserDeptLevelRole(userUuid);
	}

	@Override
	public List<UserDeptLevelRoleDto> getUserDeptLevelRole(String userUuid) {

		List<UserDeptLevelRoleDto> userDeptLevelRoleDtoList = new ArrayList<>();
		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList;

		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuidAndStatus(userUuid, true);

		for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
			userDepartmentLevelRoleEntityList = userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuidAndStatus(userDepartmentLevelEntity.getUuid(), true);
			userDeptLevelRoleDtoList.add(UserDepartmentLevelRoleAdapter.getUserDeptLevelRoleDto(userDepartmentLevelEntity, userDepartmentLevelRoleEntityList));
		}
		return userDeptLevelRoleDtoList;
	}

	@Override
	public List<RoleDto> getUserRoles(String userUuid) {
		List<RoleDto> roleDtoList = new ArrayList<>();
		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList;
		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuidAndStatus(userUuid, true);
		List<String> roleUuids;

		for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
			userDepartmentLevelRoleEntityList = userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuidAndStatus(userDepartmentLevelEntity.getUuid(), true);
			roleUuids = userDepartmentLevelRoleEntityList.parallelStream().map(UserDepartmentLevelRoleEntity::getRoleUuid).collect(Collectors.toList());
			List<RoleEntity> roleEntities = roleDbService.findByUuidInAndStatus(roleUuids, true);
			roleDtoList.addAll(RoleAdapter.getDtoList(roleEntities));
		}
		return roleDtoList;
	}

	@Override
	@Transactional
	public void revokeAllRoles(String userUuid) {

		userService.assertActiveUserByUserUuid(userUuid);

		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuid(userUuid);

		if (CollectionUtils.isEmpty(userDepartmentLevelEntityList)) {
			throw new ApiValidationException("User doesn't have any access");
		}

		userDepartmentLevelService.delete(userDepartmentLevelEntityList);

		userManagerMappingService.deleteManagerMappingForUser(userUuid);

		publishCurrentRoleSnapshot(userUuid);

	}

	@Override
	public void revokeAllRolesOfDepartmentOfLevel(String userUuid, Department department, AccessLevel accessLevel) {
		userService.assertActiveUserByUserUuid(userUuid);

		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuidAndDepartmentAndAccessLevel(userUuid, department, accessLevel);
		if (CollectionUtils.isEmpty(userDepartmentLevelEntityList)) {
			throw new ApiValidationException("User doesn't have any access in this department");
		}

		for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
			userDepartmentLevelService.delete(userDepartmentLevelEntity);
		}
		publishCurrentRoleSnapshot(userUuid);
		return;
	}

	@Override
	public void revokeAccessLevelEntityForDepartmentOfLevel(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto) {

		userService.assertActiveUserByUserUuid(addUserDeptLevelRequestDto.getUserUuid());

		userDepartmentLevelService.revokeAccessLevelEntityForDepartmentOfLevel(addUserDeptLevelRequestDto);
		publishCurrentRoleSnapshot(addUserDeptLevelRequestDto.getUserUuid());
	}

	@Override
	public void revokeRolesForDepartmentOfLevel(UserDeptLevelRoleListDto userDeptLevelRoleListDto) {
		userService.assertActiveUserByUserUuid(userDeptLevelRoleListDto.getUserUuid());

		UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelDbService.findByUserUuidAndDepartmentAndAccessLevelAndStatus(userDeptLevelRoleListDto.getUserUuid(),
				userDeptLevelRoleListDto.getDepartment(), userDeptLevelRoleListDto.getAccessLevel(), true);

		if (null == userDepartmentLevelEntity) {
			throw new ApiValidationException("Unable to revoke roles, User doesn't exist at this level in the department");
		}

		userDepartmentLevelRoleService.revokeRoles(userDepartmentLevelEntity.getUuid(), userDeptLevelRoleListDto.getRolesUuid());
		publishCurrentRoleSnapshot(userDeptLevelRoleListDto.getUserUuid());
	}

	@Override
	public Map<String, List<String>> getUsersForRoles(Department department, String roleName, List<String> accessLevelEntityList) {

		log.info("Got request to get list of userid by rolename {} and department {}", roleName, department);

		RoleDto roleDto = roleService.findByRoleNameAndDepartment(roleName, department);

		Map<String, List<String>> userIdAccessLevelIdListMap = new HashMap<>();

		if (Objects.nonNull(roleDto) && roleDto.getDepartment().equals(department)) {

			List<UserDepartmentLevelRoleEntity> departmentLevelRoleEntities = userDepartmentLevelRoleDbService.findByRoleUuid(roleDto.getUuid());

			if (CollectionUtils.isNotEmpty(departmentLevelRoleEntities)) {

				List<String> uuids = departmentLevelRoleEntities.stream().map(UserDepartmentLevelRoleEntity::getUserDepartmentLevelUuid).collect(Collectors.toList());

				List<UserDepartmentLevelEntity> departmentLevelEntities = userDepartmentLevelDbService.findByUuidInAndAccessLevel(uuids, roleDto.getAccessLevel());

				if (CollectionUtils.isNotEmpty(departmentLevelEntities)) {

					departmentLevelEntities.forEach(entity -> {

						Set<String> accessLevelUuids = new HashSet<>(Arrays.asList((entity.getCsvAccessLevelEntityUuid().split(","))));

						for (String accessLevelEntity : accessLevelEntityList) {
							if (accessLevelUuids.contains(accessLevelEntity)) {
								List<String> accessLevelIds = userIdAccessLevelIdListMap.getOrDefault(entity.getUserUuid(), new ArrayList<>());
								accessLevelIds.add(accessLevelEntity);
								userIdAccessLevelIdListMap.put(entity.getUserUuid(), accessLevelIds);
							}
						}
						// if (!Collections.disjoint(accessLevelEntityList, accessLevelUuids)) {
						// userIds.add(entity.getUserUuid());
						// }
					});
				}
			}

		}

		return userIdAccessLevelIdListMap;
	}

	@Override
	public List<UserContactDetailsResponseDto> getUserContactDetails(Department department, String roleName, List<String> accessLevelEntity) {
		List<String> userUuids = new ArrayList<>(getUsersForRoles(department, roleName, accessLevelEntity).keySet());

		if (CollectionUtils.isEmpty(userUuids)) {
			return Collections.emptyList();
		}

		List<UserEntity> userEntities = userDbService.findByUuidInAndStatus(userUuids, true);

		if (CollectionUtils.isEmpty(userEntities)) {
			return Collections.emptyList();
		}

		return userEntities.parallelStream().map(UserAdapter::convertToContactResponseDto).collect(Collectors.toList());
	}

	private void publishCurrentRoleSnapshot(String userUuid) {
		List<UserDeptLevelRoleNameUrlExpandedDto> data = aclService.getUserDeptLevelRoleNameUrlExpandedDtoBe(userUuid);
		UserRoleSnapshot userRoleSnapshot = UserRoleSnapshot.builder().userUuid(userUuid).userDeptLevelRoles(data).build();
		notificationProducer.publish(roleTopic, UserRoleSnapshot.class.getName(), userRoleSnapshot);
	}
}
