/**
 * 
 */
package com.stanzaliving.user.acl.db.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.sqljpa.service.impl.AbstractJpaServiceImpl;
import com.stanzaliving.user.acl.db.service.UserRoleDbService;
import com.stanzaliving.user.acl.entity.UserRoleEntity;
import com.stanzaliving.user.acl.repository.UserRoleRepository;

/**
 * @author naveen.kumar
 *
 * @date 23-Oct-2019
 *
 **/
@Service
public class UserRoleDbServiceImpl extends AbstractJpaServiceImpl<UserRoleEntity, Long, UserRoleRepository> implements UserRoleDbService {

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Override
	protected UserRoleRepository getJpaRepository() {
		return userRoleRepository;
	}

	@Override
	public boolean isRoleAssigned(String userId, String roleId) {
		return getJpaRepository().existsByUserIdAndRoleId(userId, roleId);
	}

	@Override
	public List<UserRoleEntity> getUserRoles(String userId) {
		Page<UserRoleEntity> page = getJpaRepository().findByUserId(userId, PageRequest.of(0, 100));

		if (page != null) {
			return page.getContent();
		}

		return new ArrayList<>();
	}

	@Override
	public void deleteUserRoles(String userId) {
		getJpaRepository().deleteByUserId(userId);
	}

}