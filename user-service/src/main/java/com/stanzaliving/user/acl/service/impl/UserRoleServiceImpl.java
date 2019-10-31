/**
 * 
 */
package com.stanzaliving.user.acl.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stanzaliving.core.user.acl.dto.RoleMetadataDto;
import com.stanzaliving.user.acl.adapters.RoleAdapter;
import com.stanzaliving.user.acl.db.service.RoleDbService;
import com.stanzaliving.user.acl.db.service.UserRoleDbService;
import com.stanzaliving.user.acl.entity.RoleEntity;
import com.stanzaliving.user.acl.entity.UserRoleEntity;
import com.stanzaliving.user.acl.service.UserRoleService;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen.kumar
 *
 * @date 23-Oct-2019
 *
 **/
@Log4j2
@Service
public class UserRoleServiceImpl implements UserRoleService {

	@Autowired
	private RoleDbService roleDbService;

	@Autowired
	private UserRoleDbService userRoleDbService;

	@Override
	@Transactional
	public void assignRoles(String userId, List<String> roleIds) {

		log.info("Assigning " + roleIds.size() + " Roles to User: " + userId);

		if (CollectionUtils.isEmpty(roleIds)) {
			log.info("Removing all roles of user: " + userId);

			userRoleDbService.deleteUserRoles(userId);

			return;
		}

		assignRolesToUser(userId, roleIds);
	}

	private void assignRolesToUser(String userId, List<String> roleIds) {

		List<RoleEntity> roleEntities = roleDbService.findByUuidIn(roleIds);

		if (CollectionUtils.isNotEmpty(roleEntities)) {

			List<UserRoleEntity> userRoleEntities = new ArrayList<>();

			for (RoleEntity roleEntity : roleEntities) {

				UserRoleEntity userRoleEntity =
						UserRoleEntity.builder()
								.userId(userId)
								.roleId(roleEntity.getUuid())
								.build();

				userRoleEntities.add(userRoleEntity);

			}

			log.info("Saving " + userRoleEntities.size() + " Roles to User: " + userId);

			userRoleDbService.deleteUserRoles(userId);
			userRoleDbService.save(userRoleEntities);
		}
	}

	@Override
	public List<RoleMetadataDto> getRolesAssignedToUser(String userId) {

		List<UserRoleEntity> userRoleEntities = userRoleDbService.getUserRoles(userId);

		if (CollectionUtils.isNotEmpty(userRoleEntities)) {

			log.info(userRoleEntities.size() + " Roles assigned to user: " + userId);

			List<String> roleIds = userRoleEntities.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList());

			List<RoleEntity> roleEntities = roleDbService.findByUuidIn(roleIds);

			return RoleAdapter.getMetadataDto(roleEntities);
		}

		log.info("No Role assigned to user: " + userId);

		return new ArrayList<>();
	}
}