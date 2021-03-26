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
import org.apache.commons.lang3.StringUtils;
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
	public List<RoleEntity> findByDepartmentAndAccessLevelAndRoleName(Department department, AccessLevel accessLevel,String roleName) {
		return getJpaRepository().findByDepartmentAndAccessLevelAndRoleName(department, accessLevel,roleName);
	}

	@Override
	public List<RoleEntity> filter(RoleDto roleDto) {
		StanzaSpecificationBuilder<RoleEntity> specificationBuilder = new StanzaSpecificationBuilder<>();

		if (StringUtils.isNotBlank(roleDto.getUuid())){
			specificationBuilder.with(QueryConstants.Role.ROLE_UUID, CriteriaOperation.EQ, roleDto.getUuid());
		}

		if (Objects.nonNull(roleDto.getAccessLevel())) {
			specificationBuilder = specificationBuilder.with(QueryConstants.Role.ACCESS_LEVEL, CriteriaOperation.ENUM_EQ, roleDto.getAccessLevel());
		}

		if (Objects.nonNull(roleDto.getDepartment())) {
			specificationBuilder = specificationBuilder.with(QueryConstants.Role.DEPARTMENT, CriteriaOperation.ENUM_EQ, roleDto.getDepartment());
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

	@Override
	public List<RoleEntity> findByRoleNameAndDepartment(List<String> roleName, Department department) {
		return getJpaRepository().findByRoleNameInAndDepartment(roleName, department);
	}

	@Override
	public RoleEntity findByRoleNameAndDepartment(String roleName,Department department) {
		return getJpaRepository().findByRoleNameAndDepartment(roleName, department);
	}

	@Override
	public boolean isRoleExists(String roleName, Department department) {
		return getJpaRepository().existsByRoleNameAndDepartment(roleName, department);
	}

}