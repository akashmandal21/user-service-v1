/**
 * 
 */
package com.stanzaliving.user.acl.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.user.acl.db.service.RoleDbService;
import com.stanzaliving.user.acl.db.service.UserRoleDbService;
import com.stanzaliving.user.acl.entity.UserRoleEntity;
import com.stanzaliving.user.acl.service.AclService;

import lombok.extern.log4j.Log4j2;

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
	private RoleDbService roleDbService;

	@Autowired
	private UserRoleDbService userRoleDbService;

	@Override
	public boolean isAccesible(String userId, String url) {

		List<UserRoleEntity> userRoleEntities = userRoleDbService.getUserRoles(userId);

		if (CollectionUtils.isNotEmpty(userRoleEntities)) {

			log.info(userRoleEntities.size() + " Roles assigned to user: " + userId);

			List<String> roleIds = userRoleEntities.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList());

			return roleDbService.isActionPresentInRoles(roleIds, url);
		}

		log.info("No Role assigned to user: " + userId);

		return false;
	}

}