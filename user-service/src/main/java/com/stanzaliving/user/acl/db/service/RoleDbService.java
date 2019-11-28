/**
 * 
 */
package com.stanzaliving.user.acl.db.service;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.user.acl.entity.RoleEntity;

import java.util.Collection;
import java.util.List;

/**
 * @author naveen
 *
 * @date 21-Oct-2019
 */
public interface RoleDbService extends AbstractJpaService<RoleEntity, Long> {

	boolean isRoleExists(String roleName, Department department, AccessLevel accessLevel);

	boolean isActionPresentInRoles(Collection<String> roleIds, String actionUrl);

	List<RoleEntity> findByDepartmentAndAccessLevel(Department department, AccessLevel accessLevel);

}