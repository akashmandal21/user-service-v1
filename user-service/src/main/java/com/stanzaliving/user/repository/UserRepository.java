/**
 * 
 */
package com.stanzaliving.user.repository;

import org.springframework.stereotype.Repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.entity.UserEntity;

import java.util.List;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Repository
public interface UserRepository extends AbstractJpaRepository<UserEntity, Long> {

	UserEntity findByMobileAndIsoCode(String mobile, String isoCode);

	List<UserEntity> findByUserProfile_FirstNameStartingWith(String firstName);
}