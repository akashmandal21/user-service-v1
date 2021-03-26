/**
 * 
 */
package com.stanzaliving.user.acl.repository;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.acl.entity.RoleEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author naveen
 *
 * @date 21-Oct-2019
 */
@Repository
public interface RoleRepository extends AbstractJpaRepository<RoleEntity, Long> {

	boolean existsByRoleName(String roleName);

	boolean existsByRoleNameAndDepartment(String roleName,Department department);

	RoleEntity findByRoleName(String roleName);
	
	RoleEntity findByRoleNameAndDepartment(String roleName, Department department);

	List<RoleEntity> findByRoleNameInAndDepartment(List<String> roleNames, Department department);

	List<RoleEntity> findByDepartmentAndAccessLevel(Department department, AccessLevel accessLevel);

	List<RoleEntity> findByDepartmentAndAccessLevelAndRoleName(Department department, AccessLevel accessLevel,
			String roleName);
}