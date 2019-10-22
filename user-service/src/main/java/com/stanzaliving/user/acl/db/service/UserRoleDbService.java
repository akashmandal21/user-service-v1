/**
 * 
 */
package com.stanzaliving.user.acl.db.service;

import java.util.List;

import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.user.acl.entity.UserRoleEntity;

/**
 * @author naveen.kumar
 *
 * @date 23-Oct-2019
 *
 **/
public interface UserRoleDbService extends AbstractJpaService<UserRoleEntity, Long> {

	boolean isRoleAssigned(String userId, String roleId);

	List<UserRoleEntity> getUserRoles(String userId);

	void deleteUserRoles(String userId);
}