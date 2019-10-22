/**
 * 
 */
package com.stanzaliving.user.acl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.acl.entity.UserRoleEntity;

/**
 * @author naveen
 *
 * @date 22-Oct-2019
 */
@Repository
public interface UserRoleRepository extends AbstractJpaRepository<UserRoleEntity, Long> {

	boolean existsByUserIdAndRoleId(String userId, String roleId);

	Page<UserRoleEntity> findByUserId(String userId, Pageable pageable);

	void deleteByUserId(String userId);
}