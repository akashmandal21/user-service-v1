/**
 * 
 */
package com.stanzaliving.user.acl.db.service.impl;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.sqljpa.service.impl.AbstractJpaServiceImpl;
import com.stanzaliving.core.sqljpa.specification.utils.CriteriaOperation;
import com.stanzaliving.core.sqljpa.specification.utils.StanzaSpecificationBuilder;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.user.acl.constants.QueryConstants;
import com.stanzaliving.user.acl.db.service.RoleDbService;
import com.stanzaliving.user.acl.entity.RoleEntity;
import com.stanzaliving.user.acl.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
	public boolean isRoleExists(String roleName) {
		return getJpaRepository().existsByRoleName(roleName);
	}

	
	
	@Override
	public List<RoleEntity> findByDepartmentAndAccessLevel(Department department, AccessLevel accessLevel) {
		return getJpaRepository().findByDepartmentAndAccessLevel(department, accessLevel);
	}

	@Override
	public List<RoleEntity> filter(RoleDto roleDto) {
		StanzaSpecificationBuilder<RoleEntity> specificationBuilder = new StanzaSpecificationBuilder<>();

		if (Objects.nonNull(roleDto.getAccessLevel())) {
			specificationBuilder = specificationBuilder.with(QueryConstants.Role.ACCESS_LEVEL, CriteriaOperation.ENUM_EQ, roleDto.getAccessLevel());
		}

		if (Objects.nonNull(roleDto.getDepartment())) {
			specificationBuilder = specificationBuilder.with(QueryConstants.Role.Department, CriteriaOperation.ENUM_EQ, roleDto.getDepartment());
		}

		if (Objects.nonNull(roleDto.getRoleName())) {
			specificationBuilder = specificationBuilder.with(QueryConstants.Role.ROLE_NAME, CriteriaOperation.LIKE, roleDto.getRoleName());
		}

		return getJpaRepository().findAll(specificationBuilder.build());
	}

	@Override
	public RoleEntity findByRoleName(String roleName) {
		return getJpaRepository().findByRoleName(roleName);
	}

}