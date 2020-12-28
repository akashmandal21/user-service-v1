package com.stanzaliving.user.acl.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.exception.ApiValidationException;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelRoleDbService;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import com.stanzaliving.user.acl.service.UserDepartmentLevelRoleService;

@Service
public class UserDepartmentLevelRoleServiceImpl implements UserDepartmentLevelRoleService {

	@Autowired
	private UserDepartmentLevelRoleDbService userDepartmentLevelRoleDbService;

	@Override
	public List<UserDepartmentLevelRoleEntity> addRoles(String userDepartmentLevelUuid, List<String> rolesUuid) {

		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityListExisting =
				userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuidAndStatus(userDepartmentLevelUuid, true);

		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityListNew =
				rolesUuid.stream()
						.map(roleUuid -> new UserDepartmentLevelRoleEntity(userDepartmentLevelUuid, roleUuid)).collect(Collectors.toList());

		TreeSet<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityTreeSet = new TreeSet<>(Comparator.comparing(UserDepartmentLevelRoleEntity::getRoleUuid));
		userDepartmentLevelRoleEntityTreeSet.addAll(userDepartmentLevelRoleEntityListExisting);
		userDepartmentLevelRoleEntityTreeSet.addAll(userDepartmentLevelRoleEntityListNew);
		return userDepartmentLevelRoleDbService.save(new ArrayList<>(userDepartmentLevelRoleEntityTreeSet));

	}

	@Override
	public void revokeRoles(String userDepartmentLevelUuid, List<String> rolesUuid) {

		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityListExisting =
				userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuidAndRoleUuidInAndStatus(userDepartmentLevelUuid, rolesUuid, true);

		if (CollectionUtils.isEmpty(userDepartmentLevelRoleEntityListExisting)) {
			throw new ApiValidationException("Roles does not belong to user");
		}

		userDepartmentLevelRoleDbService.delete(userDepartmentLevelRoleEntityListExisting);

	}

	@Override
	public List<UserDepartmentLevelRoleEntity> findByRoleUuid(String roleUuid) {

		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = userDepartmentLevelRoleDbService.findByRoleUuid(roleUuid);

		return userDepartmentLevelRoleEntityList;
	}

}