/**
 * 
 */
package com.stanzaliving.user.acl.service.impl;

import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleNameUrlExpandedDto;
import com.stanzaliving.core.user.acl.enums.RoleAccessType;
import com.stanzaliving.user.acl.db.service.impl.*;
import com.stanzaliving.user.acl.entity.*;
import com.stanzaliving.user.acl.service.AclService;
import javafx.util.Pair;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
	UserDepartmentLevelDbServiceImpl userDepartmentLevelDbService;

	@Autowired
	UserDepartmentLevelRoleDbServiceImpl userDepartmentLevelRoleDbService;

	@Autowired
	RoleAccessDbServiceImpl roleAccessDbService;

	@Autowired
	RoleDbServiceImpl roleDbService;

	@Autowired
	ApiDbServiceImpl apiDbService;

	@Override
	public boolean isAccesible(String userId, String url) {

		return false;
	}

	public List<UserDeptLevelRoleNameUrlExpandedDto> getUserDeptLevelRoleNameUrlExpandedDto(String userUuid) {

		List<UserDeptLevelRoleNameUrlExpandedDto> UserDeptLevelRoleNameUrlExpandedDtoList = new ArrayList<>();

		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuidAndStatus(userUuid, true);

		for(UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
			List<String> roleUuidList = getRoleUuidApiUuidListOfUser(userDepartmentLevelEntity).getKey();
			List<String> apiUuidList = getRoleUuidApiUuidListOfUser(userDepartmentLevelEntity).getValue();
			List<RoleEntity> roleEntityList = roleDbService.findByUuidInAndStatus(roleUuidList, true);
			List<String> roleNameList = roleEntityList.stream().map(entity -> entity.getRoleName()).collect(Collectors.toList());
			List<ApiEntity> apiEntityList = apiDbService.findByUuidInAndStatus(apiUuidList, true);
			List<String> actionUrlList = apiEntityList.stream().map(entity -> entity.getActionUrl()).collect(Collectors.toList());

			//UserDeptLevelRoleNameUrlExpandedDtoList.add(new UserDeptLevelRoleNameUrlExpandedDto(userDepartmentLevelEntity, roleEntityList, apiEntityList));



			//RoleAccessUuidView
		}

		return null;
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
			//roleAccessEntityListChild = roleAccessDbService.findByRoleUuidInAndRoleAccessTypeAndStatus(roleUuidListParent, RoleAccessType.ROLE, true);
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