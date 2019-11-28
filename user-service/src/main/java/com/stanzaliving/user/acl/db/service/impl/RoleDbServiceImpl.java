/**
 * 
 */
package com.stanzaliving.user.acl.db.service.impl;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.sqljpa.service.impl.AbstractJpaServiceImpl;
import com.stanzaliving.user.acl.db.service.RoleDbService;
import com.stanzaliving.user.acl.entity.RoleEntity;
import com.stanzaliving.user.acl.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * @author naveen
 *
 * @date 21-Oct-2019
 */
@Service
public class RoleDbServiceImpl extends AbstractJpaServiceImpl<RoleEntity, Long, RoleRepository> implements RoleDbService {

	@Autowired
	private RoleRepository roleRepository;

	@Override
	protected RoleRepository getJpaRepository() {
		return roleRepository;
	}
	
	@Override
	public boolean isRoleExists(String roleName, Department department, AccessLevel accessLevel) {
		return getJpaRepository().existsByRoleNameAndDepartmentAndAccessLevel(roleName, department, accessLevel);
	}


	@Override
	public boolean isActionPresentInRoles(Collection<String> roleIds, String actionUrl) {
		//TODO:- update below
		//return getJpaRepository().existsByUuidInAndApiEntities_ActionUrl(roleIds, actionUrl);
		return false;
	}

	@Override
	public List<RoleEntity> findByDepartmentAndAccessLevel(Department department, AccessLevel accessLevel) {
		return getJpaRepository().findByDepartmentAndAccessLevel(department, accessLevel);
	}

}