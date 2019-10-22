/**
 * 
 */
package com.stanzaliving.user.acl.repository;

import java.util.Collection;

import org.springframework.stereotype.Repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.acl.entity.RoleEntity;

/**
 * @author naveen
 *
 * @date 21-Oct-2019
 */
@Repository
public interface RoleRepository extends AbstractJpaRepository<RoleEntity, Long> {

	boolean existsByRoleName(String roleName);

	boolean existsByUuidInAndApiActionUrl(Collection<String> roleIds, String actionUrl);
}