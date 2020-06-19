/**
 * 
 */
package com.stanzaliving.user.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.core.user.enums.UserType;
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
	
	UserEntity findByMobileAndIsoCodeAndUserType(String mobile, String isoCode,UserType userType);


	/**
	 * @author piyush srivastava
	 * @param nameStartsWith String
	 * @return List
	 */
	@Query(
			"SELECT u FROM com.stanzaliving.user.entity.UserEntity u" +
			" WHERE CONCAT_WS(' ', u.userProfile.firstName, u.userProfile.middleName, u.userProfile.lastName)" +
			" LIKE :name%")
	List<UserEntity> searchByName(@Param("name") String nameStartsWith);
}