/**
 * 
 */
package com.stanzaliving.user.acl.service.impl;

import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleNameUrlExpandedDto;
import com.stanzaliving.core.user.acl.enums.RoleAccessType;
import com.stanzaliving.user.acl.adapters.UserDepartmentLevelRoleAdapter;
import com.stanzaliving.user.acl.db.service.*;
import com.stanzaliving.user.acl.entity.*;
import com.stanzaliving.user.acl.service.AclService;
import com.stanzaliving.user.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author naveen.kumar
 *
 * @date 23-Oct-2019
 *
 **/
@Log4j2
@Service
public class AclServiceImpl implements AclService {

	@Autowired
	UserDepartmentLevelDbService userDepartmentLevelDbService;

	@Autowired
	UserDepartmentLevelRoleDbService userDepartmentLevelRoleDbService;

	@Autowired
	RoleAccessDbService roleAccessDbService;

	@Autowired
	RoleDbService roleDbService;

	@Autowired
	ApiDbService apiDbService;

	@Autowired
	UserService userService;

	@Override
	public boolean isAccessible(String userId, String url) {

		Set<String> accessibleUrlList = getAccessibleUrlList(userId);
		return accessibleUrlList.contains(url);

	}

	@Override
	public List<UserDeptLevelRoleNameUrlExpandedDto> getUserDeptLevelRoleNameUrlExpandedDtoFe(String userUuid) {
		List<UserDeptLevelRoleNameUrlExpandedDto> userDeptLevelRoleNameUrlExpandedDtoList = getUserDeptLevelRoleNameUrlExpandedDto(userUuid);
		for(UserDeptLevelRoleNameUrlExpandedDto userDeptLevelRoleNameUrlExpandedDto : userDeptLevelRoleNameUrlExpandedDtoList) {
			userDeptLevelRoleNameUrlExpandedDto.setUrlList(new ArrayList<>());
		}
		return userDeptLevelRoleNameUrlExpandedDtoList;
	}

	@Override
	public List<UserDeptLevelRoleNameUrlExpandedDto> getUserDeptLevelRoleNameUrlExpandedDtoBe(String userUuid) {
		List<UserDeptLevelRoleNameUrlExpandedDto> userDeptLevelRoleNameUrlExpandedDtoList = getUserDeptLevelRoleNameUrlExpandedDto(userUuid);
		for(UserDeptLevelRoleNameUrlExpandedDto userDeptLevelRoleNameUrlExpandedDto : userDeptLevelRoleNameUrlExpandedDtoList) {
			userDeptLevelRoleNameUrlExpandedDto.setRolesList(new ArrayList<>());
		}
		return userDeptLevelRoleNameUrlExpandedDtoList;
	}

	@Override
	public Set<String> getAccessibleUrlList(String userUuid) {
		List<UserDeptLevelRoleNameUrlExpandedDto> userDeptLevelRoleNameUrlExpandedDtoList = getUserDeptLevelRoleNameUrlExpandedDto(userUuid);
		Set<String> accessibleUrlList = new HashSet<>();
		for (UserDeptLevelRoleNameUrlExpandedDto userDeptLevelRoleNameUrlExpandedDto : userDeptLevelRoleNameUrlExpandedDtoList) {
			accessibleUrlList.addAll(userDeptLevelRoleNameUrlExpandedDto.getUrlList());
		}
		return accessibleUrlList;
	}

	private List<UserDeptLevelRoleNameUrlExpandedDto> getUserDeptLevelRoleNameUrlExpandedDto(String userUuid) {

		userService.assertActiveUserByUserUuid(userUuid);

		List<UserDeptLevelRoleNameUrlExpandedDto> userDeptLevelRoleNameUrlExpandedDtoList = new ArrayList<>();

		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuidAndStatus(userUuid, true);

		for(UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
			Pair<List<String>, List<String>> roleUuidApiUuidList = getRoleUuidApiUuidListOfUser(userDepartmentLevelEntity);
			List<RoleEntity> roleEntityList = roleDbService.findByUuidInAndStatus(roleUuidApiUuidList.getKey(), true);
			List<ApiEntity> apiEntityList = apiDbService.findByUuidInAndStatus(roleUuidApiUuidList.getValue(), true);

			userDeptLevelRoleNameUrlExpandedDtoList.add(
					UserDepartmentLevelRoleAdapter.getUserDeptLevelRoleNameUrlExpandedDto(userDepartmentLevelEntity, roleEntityList, apiEntityList));
		}

		return userDeptLevelRoleNameUrlExpandedDtoList;
	}


	private Pair<List<String>, List<String>> getRoleUuidApiUuidListOfUser(UserDepartmentLevelEntity userDepartmentLevelEntity) {

		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuidAndStatus(userDepartmentLevelEntity.getUuid(), true);
		Set<String> finalRoleUuidSet = new HashSet<>();
		Set<String> finalApiUuidSet = new HashSet<>();

		List<String> roleUuidListParent = new ArrayList<>(userDepartmentLevelRoleEntityList.stream().map(entity -> entity.getRoleUuid()).collect(Collectors.toSet()));
		finalRoleUuidSet.addAll(roleUuidListParent);

		List<RoleAccessEntity> roleAccessEntityListChild;
		Set<String> roleUuidSetChild;
		Set<String> apiUuidSetChild;

		while(CollectionUtils.isNotEmpty(roleUuidListParent)) {
			roleAccessEntityListChild = roleAccessDbService.findByRoleUuidInAndStatus(roleUuidListParent, true);
			roleUuidSetChild = roleAccessEntityListChild.stream().filter(entity -> RoleAccessType.ROLE.equals(entity.getRoleAccessType())).map(entity -> entity.getAccessUuid()).collect(Collectors.toSet());
			apiUuidSetChild = roleAccessEntityListChild.stream().filter(entity -> RoleAccessType.API.equals(entity.getRoleAccessType())).map(entity -> entity.getAccessUuid()).collect(Collectors.toSet());
			roleUuidListParent = roleUuidSetChild.stream().filter(child -> !finalRoleUuidSet.contains(child)).collect(Collectors.toList());
			finalRoleUuidSet.addAll(roleUuidListParent);
			finalApiUuidSet.addAll(apiUuidSetChild);
		}

		return new Pair<>(new ArrayList<>(finalRoleUuidSet), new ArrayList<>(finalApiUuidSet));

	}
}