/**
 * 
 */
package com.stanzaliving.user.repository;

import org.springframework.stereotype.Repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.entity.BlacklistUserEntity;

@Repository
public interface BlacklistUserRepository extends AbstractJpaRepository<BlacklistUserEntity, Long> {

	boolean existsByMobileAndStatus(String mobile,boolean status);
	
	BlacklistUserEntity findByMobile(String mobile);	
	
}