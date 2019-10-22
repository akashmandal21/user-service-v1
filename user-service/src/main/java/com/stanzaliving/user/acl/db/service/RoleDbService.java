/**
 * 
 */
package com.stanzaliving.user.acl.db.service;

import java.util.Collection;

import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.user.acl.entity.RoleEntity;

/**
 * @author naveen
 *
 * @date 21-Oct-2019
 */
public interface RoleDbService extends AbstractJpaService<RoleEntity, Long> {

	boolean isRoleExists(String roleName);

	boolean isActionPresentInRoles(Collection<String> roleIds, String actionUrl);

}