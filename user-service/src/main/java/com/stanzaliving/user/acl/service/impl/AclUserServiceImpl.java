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

import com.stanzaliving.core.transformation.client.cache.TransformationCache;
import com.stanzaliving.core.user.acl.dto.UserAccessLevelIdsByRoleNameWithFiltersDto;
import com.stanzaliving.core.user.acl.dto.UserAccessLevelListDto;
import com.stanzaliving.core.user.acl.dto.UsersByFiltersRequestDto;
import com.stanzaliving.core.user.acl.dto.UsersByFiltersResponseDto;
import com.stanzaliving.core.user.acl.enums.Role;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleByEmailRequestDto;
import com.stanzaliving.user.acl.repository.RoleRepository;
import com.stanzaliving.user.acl.repository.UserDepartmentLevelRepository;
import com.stanzaliving.user.acl.repository.UserDepartmentLevelRoleRepository;
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

	@Autowired
	private UserDepartmentLevelRepository userDepartmentLevelRepository;

	@Autowired
	private TransformationCache transformationCache;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserDepartmentLevelRoleRepository userDepartmentLevelRoleRepository;

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

	@Override
	public void bulkAddRole(AddUserDeptLevelRoleByEmailRequestDto addUserDeptLevelRoleByEmailRequestDto) {
		Map<String,String> userUuids = userDbService.getUuidByEmail(addUserDeptLevelRoleByEmailRequestDto.getEmails());

		addUserDeptLevelRoleByEmailRequestDto.getEmails()
						.forEach(email->{
							if (!userUuids.keySet().contains(email)){
								throw new ApiValidationException("No user exists with email id: "+email);
							}
						});

		userUuids
				.forEach((email,uuid) -> {
							addRole(
									AddUserDeptLevelRoleRequestDto
											.builder()
											.rolesUuid(addUserDeptLevelRoleByEmailRequestDto.getRolesUuid())
											.userUuid(uuid)
											.department(addUserDeptLevelRoleByEmailRequestDto.getDepartment())
											.accessLevel(addUserDeptLevelRoleByEmailRequestDto.getAccessLevel())
											.accessLevelEntityListUuid(addUserDeptLevelRoleByEmailRequestDto.getAccessLevelEntityListUuid())
											.build()
							);
						}
				);
	}

	private void publishCurrentRoleSnapshot(String userUuid) {
		List<UserDeptLevelRoleNameUrlExpandedDto> data = aclService.getUserDeptLevelRoleNameUrlExpandedDtoBe(userUuid);
		UserRoleSnapshot userRoleSnapshot = UserRoleSnapshot.builder().userUuid(userUuid).userDeptLevelRoles(data).build();
		notificationProducer.publish(roleTopic, UserRoleSnapshot.class.getName(), userRoleSnapshot);
	}

	@Override
	public Map<String, List<String>> getUsersForRolesWithFilters(UserAccessLevelIdsByRoleNameWithFiltersDto requestDto) {

		log.info("Got request to get list of userid by rolename {} and department {}", requestDto.getRoleName(), requestDto.getDepartment());
		log.info("UserAccessLevelIdsByRoleNameWithFiltersDto : {}", requestDto);

		RoleDto roleDto = roleService.findByRoleNameAndDepartment(requestDto.getRoleName(), requestDto.getDepartment());

		Map<String, List<String>> userIdAccessLevelIdListMap = new HashMap<>();

		if (Objects.nonNull(roleDto) && roleDto.getDepartment().equals(requestDto.getDepartment())) {

			List<UserDepartmentLevelRoleEntity> departmentLevelRoleEntities = userDepartmentLevelRoleDbService.findByRoleUuid(roleDto.getUuid());

			if (CollectionUtils.isNotEmpty(departmentLevelRoleEntities)) {

				List<String> uuids = departmentLevelRoleEntities.stream().map(UserDepartmentLevelRoleEntity::getUserDepartmentLevelUuid).collect(Collectors.toList());

				List<UserDepartmentLevelEntity> departmentLevelEntities = userDepartmentLevelDbService.findByUuidInAndAccessLevel(uuids, roleDto.getAccessLevel());

				if (CollectionUtils.isNotEmpty(departmentLevelEntities)) {

					departmentLevelEntities.forEach(entity -> {

						Set<String> accessLevelUuids = new HashSet<>(Arrays.asList((entity.getCsvAccessLevelEntityUuid().split(","))));

						for (String accessLevelEntity : requestDto.getAccessLevelId()) {
							if (accessLevelUuids.contains(accessLevelEntity)) {
								List<String> accessLevelIds = userIdAccessLevelIdListMap.getOrDefault(entity.getUserUuid(), new ArrayList<>());
								accessLevelIds.add(accessLevelEntity);
								userIdAccessLevelIdListMap.put(entity.getUserUuid(), accessLevelIds);
							}
						}
					});
				}
			}

		}
		if (CollectionUtils.isNotEmpty(requestDto.getCityFilterUuids()) || CollectionUtils.isNotEmpty(requestDto.getMicromarketFilterUuids())
			|| CollectionUtils.isNotEmpty(requestDto.getResidenceFilterUuids()) || CollectionUtils.isNotEmpty(requestDto.getCityLeadFilterUuids())
			|| CollectionUtils.isNotEmpty(requestDto.getClusterManagerFilterUuids())
			|| CollectionUtils.isNotEmpty(requestDto.getSalesAssociateFilterUuids())) {

			UsersByFiltersRequestDto usersByFiltersRequestDto = UsersByFiltersRequestDto.builder().accessLevel(requestDto.getAccessLevel())
				.cityFilterUuids(requestDto.getCityFilterUuids()).micromarketFilterUuids(requestDto.getMicromarketFilterUuids())
				.residenceFilterUuids(requestDto.getResidenceFilterUuids()).cityLeadFilterUuids(requestDto.getCityLeadFilterUuids())
				.clusterManagerFilterUuids(requestDto.getClusterManagerFilterUuids()).salesAssociateFilterUuids(requestDto.getSalesAssociateFilterUuids()).build();

			UsersByFiltersResponseDto usersByFiltersResponseDto = getUsersWithFilters(usersByFiltersRequestDto);
			List<String> userUuidsWithFilters = new ArrayList<>();
			if (usersByFiltersRequestDto.getAccessLevel() == AccessLevel.CITY && Objects.nonNull(usersByFiltersResponseDto)) {
				userUuidsWithFilters = usersByFiltersResponseDto.getCityHeadUuids();
			} else if (usersByFiltersRequestDto.getAccessLevel() == AccessLevel.MICROMARKET && Objects.nonNull(usersByFiltersResponseDto)) {
				userUuidsWithFilters = usersByFiltersResponseDto.getClusterManagerUuids();
			} else if (usersByFiltersRequestDto.getAccessLevel() == AccessLevel.RESIDENCE && Objects.nonNull(usersByFiltersResponseDto)) {
				userUuidsWithFilters = usersByFiltersResponseDto.getSalesAssociateUuids();
			}
			Map<String, List<String>> userIdAccessLevelIdListMapWithFilters = new HashMap<>();
			for (String uuid : userIdAccessLevelIdListMap.keySet()) {
				if (CollectionUtils.isNotEmpty(userUuidsWithFilters) && userUuidsWithFilters.contains(uuid)) {
					userIdAccessLevelIdListMapWithFilters.put(uuid, userIdAccessLevelIdListMap.get(uuid));
				}
			}
			return userIdAccessLevelIdListMapWithFilters;
		}
		return userIdAccessLevelIdListMap;
	}


	private UsersByFiltersResponseDto getUsersWithFilters(UsersByFiltersRequestDto filtersRequestDto) {

		if (filtersRequestDto.getAccessLevel() == AccessLevel.CITY) {
			return getCityLeadsWithFilters(filtersRequestDto);
		}
        if (filtersRequestDto.getAccessLevel() == AccessLevel.MICROMARKET) {
            return getClusterManagersWithFilters(filtersRequestDto);
        }
        if (filtersRequestDto.getAccessLevel() == AccessLevel.RESIDENCE) {
            return getSalesAssociatesWithFilters(filtersRequestDto);
        }
		return null;
	}

	private UsersByFiltersResponseDto getCityLeadsWithFilters(UsersByFiltersRequestDto filtersRequestDto) {
		log.info("Get City Leads with Filters");
		List<String> cityUuidsWithEntityFilters = getCityUuidsWithEntityFilters(filtersRequestDto);
		List<String> cityUuidsWithUserFilters = getCityUuidsWithUserFilters(filtersRequestDto);
		List<String> cityFilters = new ArrayList<>();
		if (CollectionUtils.isEmpty(cityUuidsWithEntityFilters) && CollectionUtils.isNotEmpty(cityUuidsWithUserFilters)) {
			cityFilters = cityUuidsWithUserFilters;
		}
		if (CollectionUtils.isEmpty(cityUuidsWithUserFilters) && CollectionUtils.isNotEmpty(cityUuidsWithEntityFilters)) {
			cityFilters = cityUuidsWithEntityFilters;
		}
		if (CollectionUtils.isNotEmpty(cityUuidsWithEntityFilters) && CollectionUtils.isNotEmpty(cityUuidsWithUserFilters)) {
			cityFilters = cityUuidsWithEntityFilters.stream().distinct().filter(cityUuidsWithUserFilters::contains).collect(Collectors.toList());
		}
		List<String> allCityLeads = new ArrayList<>();
		UsersByFiltersResponseDto usersByFiltersResponseDto = new UsersByFiltersResponseDto();
		if (CollectionUtils.isEmpty(cityFilters)) {
			for (UserAccessLevelListDto userAccessLevelListDto : getAllCityLeadManagers()) {
				allCityLeads.add(userAccessLevelListDto.getUserUuid());
			}
		} else {
			for (UserAccessLevelListDto userAccessLevelListDto : getAllCityLeadManagers()) {
				for (String accessLevelId : userAccessLevelListDto.getAccessLevelIds()) {
					if (cityFilters.contains(accessLevelId)) {
						allCityLeads.add(userAccessLevelListDto.getUserUuid());
						break;
					}
				}
			}
		}
		usersByFiltersResponseDto.setCityHeadUuids(allCityLeads);
		log.info("Users by filters : {}", usersByFiltersResponseDto);
		return usersByFiltersResponseDto;
	}

    private UsersByFiltersResponseDto getClusterManagersWithFilters(UsersByFiltersRequestDto filtersRequestDto) {
        log.info("Get Cluster Managers with Filters");
        List<String> micromarketUuidsWithEntityFilters = getMicromarketUuidsWithEntityFilters(filtersRequestDto);
        List<String> micromarketUuidsWithUserFilters = getMicromarketUuidsWithUserFilters(filtersRequestDto);
        List<String> micromarketFilters = new ArrayList<>();
        if (CollectionUtils.isEmpty(micromarketUuidsWithEntityFilters) && CollectionUtils.isNotEmpty(micromarketUuidsWithUserFilters)) {
            micromarketFilters = micromarketUuidsWithUserFilters;
        }
        if (CollectionUtils.isEmpty(micromarketUuidsWithUserFilters) && CollectionUtils.isNotEmpty(micromarketUuidsWithEntityFilters)) {
            micromarketFilters = micromarketUuidsWithEntityFilters;
        }
        if (CollectionUtils.isNotEmpty(micromarketUuidsWithEntityFilters) && CollectionUtils.isNotEmpty(micromarketUuidsWithUserFilters)) {
            micromarketFilters = micromarketUuidsWithEntityFilters.stream().distinct().filter(micromarketUuidsWithUserFilters::contains).collect(Collectors.toList());
        }
        List<String> allClusterManagers = new ArrayList<>();
        UsersByFiltersResponseDto usersByFiltersResponseDto = new UsersByFiltersResponseDto();
        if (CollectionUtils.isEmpty(micromarketFilters)) {
            for (UserAccessLevelListDto userAccessLevelListDto : getAllMicromarketLeadManagers()) {
                allClusterManagers.add(userAccessLevelListDto.getUserUuid());
            }
        } else {
            for (UserAccessLevelListDto userAccessLevelListDto : getAllMicromarketLeadManagers()) {
                for (String accessLevelId : userAccessLevelListDto.getAccessLevelIds()) {
                    if (micromarketFilters.contains(accessLevelId)) {
                        allClusterManagers.add(userAccessLevelListDto.getUserUuid());
                        break;
                    }
                }
            }
        }
        usersByFiltersResponseDto.setClusterManagerUuids(allClusterManagers);
		log.info("Users by filters : {}", usersByFiltersResponseDto);
        return usersByFiltersResponseDto;
    }

    private UsersByFiltersResponseDto getSalesAssociatesWithFilters(UsersByFiltersRequestDto filtersRequestDto) {
        log.info("Get Sales Associates with Filters");
        List<String> residenceUuidsWithEntityFilters = getResidenceUuidsWithEntityFilters(filtersRequestDto);
        List<String> residenceUuidsWithUserFilters = getResidenceUuidsWithUserFilters(filtersRequestDto);
        List<String> residenceFilters = new ArrayList<>();
        if (CollectionUtils.isEmpty(residenceUuidsWithEntityFilters) && CollectionUtils.isNotEmpty(residenceUuidsWithUserFilters)) {
            residenceFilters  = residenceUuidsWithUserFilters;
        }
        if (CollectionUtils.isEmpty(residenceUuidsWithUserFilters) && CollectionUtils.isNotEmpty(residenceUuidsWithEntityFilters)) {
            residenceFilters = residenceUuidsWithEntityFilters;
        }
        if (CollectionUtils.isNotEmpty(residenceUuidsWithEntityFilters) && CollectionUtils.isNotEmpty(residenceUuidsWithUserFilters)) {
            residenceFilters = residenceUuidsWithEntityFilters.stream().distinct().filter(residenceUuidsWithUserFilters::contains).collect(Collectors.toList());
        }
        List<String> allSalesAssociates = new ArrayList<>();
        UsersByFiltersResponseDto usersByFiltersResponseDto = new UsersByFiltersResponseDto();
        if (CollectionUtils.isEmpty(residenceFilters)) {
            for (UserAccessLevelListDto userAccessLevelListDto : getAllResidenceLeadManagers()) {
                allSalesAssociates.add(userAccessLevelListDto.getUserUuid());
            }
        } else {
            for (UserAccessLevelListDto userAccessLevelListDto : getAllResidenceLeadManagers()) {
                for (String accessLevelId : userAccessLevelListDto.getAccessLevelIds()) {
                    if (residenceFilters.contains(accessLevelId)) {
                        allSalesAssociates.add(userAccessLevelListDto.getUserUuid());
                        break;
                    }
                }
            }
        }
        usersByFiltersResponseDto.setSalesAssociateUuids(allSalesAssociates);
		log.info("Users by filters : {}", usersByFiltersResponseDto);
        return usersByFiltersResponseDto;
    }

	private List<String> getCityUuidsWithEntityFilters(UsersByFiltersRequestDto filtersRequestDto) {
		log.info("Get City uuids with entity filters");
		List<String> cityUuids = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(filtersRequestDto.getResidenceFilterUuids())) {
			for (String residenceUuid : filtersRequestDto.getResidenceFilterUuids()) {
				cityUuids.add(transformationCache.getCityUuidByResidenceUuid(residenceUuid));
			}
			filtersRequestDto.setCityFilterUuids(cityUuids);
		}
		if (CollectionUtils.isEmpty(cityUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getMicromarketFilterUuids())) {
			for (String micromarketUuid : filtersRequestDto.getMicromarketFilterUuids()) {
				cityUuids.add(transformationCache.getCityUuidByMicromarketUuid(micromarketUuid));
			}
			filtersRequestDto.setCityFilterUuids(cityUuids);
		}
		if (CollectionUtils.isEmpty(cityUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getCityFilterUuids())) {
			cityUuids.addAll(filtersRequestDto.getCityFilterUuids());
			filtersRequestDto.setCityFilterUuids(cityUuids);
		}
		log.info("City Uuids with entity filters : {}", cityUuids);
		return cityUuids;
	}

	private List<String> getCityUuidsWithUserFilters(UsersByFiltersRequestDto filtersRequestDto) {
		log.info("Get City uuids with user filters");
		List<String> cityUuids = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(filtersRequestDto.getSalesAssociateFilterUuids())) {
			List<String> accessLevelIds = new ArrayList<>();
			List<UserAccessLevelListDto> allSalesAssociates = getAllResidenceLeadManagers();
			for (UserAccessLevelListDto userAccessLevelListDto : allSalesAssociates) {
				if (filtersRequestDto.getSalesAssociateFilterUuids().contains(userAccessLevelListDto.getUserUuid())) {
					accessLevelIds.addAll(userAccessLevelListDto.getAccessLevelIds());
				}
			}
			for (String residenceUuid : accessLevelIds) {
				cityUuids.add(transformationCache.getCityUuidByResidenceUuid(residenceUuid));
			}
		}
		if (CollectionUtils.isEmpty(cityUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getClusterManagerFilterUuids())) {
			List<String> accessLevelIds = new ArrayList<>();
			List<UserAccessLevelListDto> allClusterManagers = getAllMicromarketLeadManagers();
			for (UserAccessLevelListDto userAccessLevelListDto : allClusterManagers) {
				if (filtersRequestDto.getClusterManagerFilterUuids().contains(userAccessLevelListDto.getUserUuid())) {
					accessLevelIds.addAll(userAccessLevelListDto.getAccessLevelIds());
				}
			}
			for (String micromarketUuid : accessLevelIds) {
				cityUuids.add(transformationCache.getCityUuidByMicromarketUuid(micromarketUuid));
			}
		}
		log.info("City Uuids with user filters : {}", cityUuids);
		return cityUuids;
	}

    private List<String> getMicromarketUuidsWithEntityFilters(UsersByFiltersRequestDto filtersRequestDto) {
        log.info("Get micromarket with entity filters");
        List<String> micromarketUuids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(filtersRequestDto.getResidenceFilterUuids())) {
            for (String residenceUuid : filtersRequestDto.getResidenceFilterUuids()) {
                micromarketUuids.add(transformationCache.getMicromarketUuidByResidenceUuid(residenceUuid));
            }
            filtersRequestDto.setMicromarketFilterUuids(micromarketUuids);
        }
        if (CollectionUtils.isEmpty(micromarketUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getMicromarketFilterUuids())) {
            micromarketUuids.addAll(filtersRequestDto.getMicromarketFilterUuids());
            filtersRequestDto.setMicromarketFilterUuids(micromarketUuids);
        }
        if (CollectionUtils.isEmpty(micromarketUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getCityFilterUuids())) {
            for (String cityUuid : filtersRequestDto.getCityFilterUuids()) {
                micromarketUuids.addAll(transformationCache.getMicromarketUuidsByCityUuid(cityUuid));
            }
            filtersRequestDto.setMicromarketFilterUuids(micromarketUuids);
        }
		log.info("Micromarket Uuids with entity filters : {}", micromarketUuids);
        return micromarketUuids;
    }

    private List<String> getMicromarketUuidsWithUserFilters(UsersByFiltersRequestDto filtersRequestDto) {
        log.info("Get Micromarket uuids with user filters");
        List<String> micromarketUuids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(filtersRequestDto.getSalesAssociateFilterUuids())) {
            List<String> accessLevelIds = new ArrayList<>();
            List<UserAccessLevelListDto> allSalesAssociates = getAllResidenceLeadManagers();
            for (UserAccessLevelListDto userAccessLevelListDto : allSalesAssociates) {
            	if (filtersRequestDto.getSalesAssociateFilterUuids().contains(userAccessLevelListDto.getUserUuid())) {
					accessLevelIds.addAll(userAccessLevelListDto.getAccessLevelIds());
				}
            }
            for (String residenceUuid : accessLevelIds) {
                micromarketUuids.add(transformationCache.getMicromarketUuidByResidenceUuid(residenceUuid));
            }
        }
        if (CollectionUtils.isEmpty(micromarketUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getCityLeadFilterUuids())) {
            List<String> accessLevelIds = new ArrayList<>();
            List<UserAccessLevelListDto> allCityLeads = getAllCityLeadManagers();
            for (UserAccessLevelListDto userAccessLevelListDto : allCityLeads) {
				if (filtersRequestDto.getCityLeadFilterUuids().contains(userAccessLevelListDto.getUserUuid())) {
					accessLevelIds.addAll(userAccessLevelListDto.getAccessLevelIds());
				}
            }
            for (String cityUuid : accessLevelIds) {
                micromarketUuids.addAll(transformationCache.getMicromarketUuidsByCityUuid(cityUuid));
            }
        }
		log.info("MM Uuids with user filters : {}", micromarketUuids);
        return micromarketUuids;
    }

    private List<String> getResidenceUuidsWithEntityFilters(UsersByFiltersRequestDto filtersRequestDto) {
        log.info("Get residences with entity filters");
        List<String> residenceUuids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(filtersRequestDto.getResidenceFilterUuids())) {
            residenceUuids.addAll(filtersRequestDto.getResidenceFilterUuids());
            filtersRequestDto.setResidenceFilterUuids(residenceUuids);
        }
        if (CollectionUtils.isEmpty(residenceUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getMicromarketFilterUuids())) {
            for (String micromarketUuid : filtersRequestDto.getMicromarketFilterUuids()) {
                residenceUuids.addAll(transformationCache.getResidenceUuidsByMicromarketUuid(micromarketUuid));
            }
            filtersRequestDto.setResidenceFilterUuids(residenceUuids);
        }
        if (CollectionUtils.isEmpty(residenceUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getCityFilterUuids())) {
            for (String cityUuid : filtersRequestDto.getCityFilterUuids()) {
                residenceUuids.addAll(transformationCache.getResidenceUuidsByCityUuid(cityUuid));
            }
            filtersRequestDto.setResidenceFilterUuids(residenceUuids);
        }
		log.info("Residence Uuids with entity filters : {}", residenceUuids);
        return residenceUuids;
    }

    private List<String> getResidenceUuidsWithUserFilters(UsersByFiltersRequestDto filtersRequestDto) {
        log.info("Get Residence uuids with user filters");
        List<String> residenceUuids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(filtersRequestDto.getClusterManagerFilterUuids())) {
            List<String> accessLevelIds = new ArrayList<>();
            List<UserAccessLevelListDto> allClusterManagers = getAllMicromarketLeadManagers();
            for (UserAccessLevelListDto userAccessLevelListDto : allClusterManagers) {
            	if (filtersRequestDto.getClusterManagerFilterUuids().contains(userAccessLevelListDto.getUserUuid())) {
					accessLevelIds.addAll(userAccessLevelListDto.getAccessLevelIds());
				}
            }
            for (String micromarketUuid : accessLevelIds) {
                residenceUuids.addAll(transformationCache.getResidenceUuidsByMicromarketUuid(micromarketUuid));
            }
        }
        if (CollectionUtils.isEmpty(residenceUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getCityLeadFilterUuids())) {
            List<String> accessLevelIds = new ArrayList<>();
            List<UserAccessLevelListDto> allCityLeads = getAllCityLeadManagers();
            for (UserAccessLevelListDto userAccessLevelListDto : allCityLeads) {
				if (filtersRequestDto.getCityLeadFilterUuids().contains(userAccessLevelListDto.getUserUuid())) {
					accessLevelIds.addAll(userAccessLevelListDto.getAccessLevelIds());
				}
            }
            for (String cityUuid : accessLevelIds) {
                residenceUuids.addAll(transformationCache.getResidenceUuidsByCityUuid(cityUuid));
            }
        }
		log.info("Residence Uuids with user filters : {}", residenceUuids);
        return residenceUuids;
    }

	private List<UserAccessLevelListDto> getAllCityLeadManagers() {
		log.info("Get All City Lead Managers");
        List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = new ArrayList<>();
        List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = new ArrayList<>();
		List<RoleEntity> roleEntityList = roleRepository.findByRoleNameInAndDepartment(Arrays.asList(Role.CITY_LEAD_MANAGER.getRoleName(), Role.CITY_APARTMENT_LEAD_MANAGER.getRoleName()),
			Department.SALES);
		for (RoleEntity roleEntity : roleEntityList) {
            List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntities = userDepartmentLevelRoleRepository.findByRoleUuid(roleEntity.getUuid());
            if (CollectionUtils.isNotEmpty(userDepartmentLevelRoleEntities)) {
                userDepartmentLevelRoleEntityList.addAll(userDepartmentLevelRoleEntities);
            }
		}
		if (CollectionUtils.isNotEmpty(userDepartmentLevelRoleEntityList)) {
		    for (UserDepartmentLevelRoleEntity userDepartmentLevelRoleEntity : userDepartmentLevelRoleEntityList) {
		        UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelRepository.findByUuid(userDepartmentLevelRoleEntity.getUserDepartmentLevelUuid());
		        if (Objects.nonNull(userDepartmentLevelEntity)) {
                    userDepartmentLevelEntityList.add(userDepartmentLevelEntity);
                }
            }
        }
		List<UserAccessLevelListDto> userAccessLevelListDtoList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(userDepartmentLevelEntityList)) {
			for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
				String[] accessLevelIdArray = userDepartmentLevelEntity.getCsvAccessLevelEntityUuid().split(",");
				UserAccessLevelListDto userAccessLevelListDto = new UserAccessLevelListDto();
				userAccessLevelListDto.setUserUuid(userDepartmentLevelEntity.getUserUuid());
				userAccessLevelListDto.setAccessLevelIds(Arrays.asList(accessLevelIdArray));
				userAccessLevelListDtoList.add(userAccessLevelListDto);
			}
		}
		log.info("Number of City Lead Managers : {}", userAccessLevelListDtoList.size());
		return userAccessLevelListDtoList;
	}

	private List<UserAccessLevelListDto> getAllMicromarketLeadManagers() {
		log.info("Get All Micromarket Lead Managers");
        List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = new ArrayList<>();
        List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = new ArrayList<>();
        List<RoleEntity> roleEntityList = roleRepository.findByRoleNameInAndDepartment(Arrays.asList(Role.MICROMARKET_LEAD_MANAGER.getRoleName(), Role.MICROMARKET_APARTMENT_LEAD_MANAGER.getRoleName()),
            Department.SALES);
        for (RoleEntity roleEntity : roleEntityList) {
            List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntities = userDepartmentLevelRoleRepository.findByRoleUuid(roleEntity.getUuid());
            if (CollectionUtils.isNotEmpty(userDepartmentLevelRoleEntities)) {
                userDepartmentLevelRoleEntityList.addAll(userDepartmentLevelRoleEntities);
            }
        }
        if (CollectionUtils.isNotEmpty(userDepartmentLevelRoleEntityList)) {
            for (UserDepartmentLevelRoleEntity userDepartmentLevelRoleEntity : userDepartmentLevelRoleEntityList) {
                UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelRepository.findByUuid(userDepartmentLevelRoleEntity.getUserDepartmentLevelUuid());
                if (Objects.nonNull(userDepartmentLevelEntity)) {
                    userDepartmentLevelEntityList.add(userDepartmentLevelEntity);
                }
            }
        }
		List<UserAccessLevelListDto> userAccessLevelListDtoList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(userDepartmentLevelEntityList)) {
			for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
				String[] accessLevelIdArray = userDepartmentLevelEntity.getCsvAccessLevelEntityUuid().split(",");
				UserAccessLevelListDto userAccessLevelListDto = new UserAccessLevelListDto();
				userAccessLevelListDto.setUserUuid(userDepartmentLevelEntity.getUserUuid());
				userAccessLevelListDto.setAccessLevelIds(Arrays.asList(accessLevelIdArray));
				userAccessLevelListDtoList.add(userAccessLevelListDto);
			}
		}
		log.info("Number of Micromarket Lead Managers : {}", userAccessLevelListDtoList.size());
		return userAccessLevelListDtoList;
	}

	private List<UserAccessLevelListDto> getAllResidenceLeadManagers() {
		log.info("Get All Residence Lead Managers");
        List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = new ArrayList<>();
        List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = new ArrayList<>();
        List<RoleEntity> roleEntityList = roleRepository.findByRoleNameInAndDepartment(Arrays.asList(Role.RESIDENCE_LEAD_MANAGER.getRoleName(), Role.RESIDENCE_APARTMENT_LEAD_MANAGER.getRoleName()),
            Department.SALES);
        for (RoleEntity roleEntity : roleEntityList) {
            List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntities = userDepartmentLevelRoleRepository.findByRoleUuid(roleEntity.getUuid());
            if (CollectionUtils.isNotEmpty(userDepartmentLevelRoleEntities)) {
                userDepartmentLevelRoleEntityList.addAll(userDepartmentLevelRoleEntities);
            }
        }
        if (CollectionUtils.isNotEmpty(userDepartmentLevelRoleEntityList)) {
            for (UserDepartmentLevelRoleEntity userDepartmentLevelRoleEntity : userDepartmentLevelRoleEntityList) {
                UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelRepository.findByUuid(userDepartmentLevelRoleEntity.getUserDepartmentLevelUuid());
                if (Objects.nonNull(userDepartmentLevelEntity)) {
                    userDepartmentLevelEntityList.add(userDepartmentLevelEntity);
                }
            }
        }
		List<UserAccessLevelListDto> userAccessLevelListDtoList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(userDepartmentLevelEntityList)) {
			for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
				String[] accessLevelIdArray = userDepartmentLevelEntity.getCsvAccessLevelEntityUuid().split(",");
				UserAccessLevelListDto userAccessLevelListDto = new UserAccessLevelListDto();
				userAccessLevelListDto.setUserUuid(userDepartmentLevelEntity.getUserUuid());
				userAccessLevelListDto.setAccessLevelIds(Arrays.asList(accessLevelIdArray));
				userAccessLevelListDtoList.add(userAccessLevelListDto);
			}
		}
		log.info("Number of Residence Lead Managers : {}", userAccessLevelListDtoList.size());
		return userAccessLevelListDtoList;
	}
}
