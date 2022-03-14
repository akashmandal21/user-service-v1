/**
 * 
 */
package com.stanzaliving.user.db.service;

import com.stanzaliving.core.base.enums.Department;
import org.springframework.data.jpa.domain.Specification;

import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.user.entity.UserEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface UserDbService extends AbstractJpaService<UserEntity, Long> {
	
	UserEntity getUserForMobile(String mobile, String isoCode);

	Specification<UserEntity> getSearchQuery(UserFilterDto userFilterDto);

	List<UserEntity> findByEmail(String email);

	UserEntity findByMobileAndUserType(String userMobile,UserType userType);

	List<UserEntity> findByUserType(UserType userType);
	
	UserEntity findByMobile(String mobile);

	UserEntity findByUuid(String uuid);

	UserEntity findByUuidAndEmail(String userUuid, String email);

	List<UserEntity> findByMobileIn(Set<String> mobileNos);

	UserEntity findTop1ByEmailOrderByCreatedAtDesc(String email);

	Map<String, String> getUuidByEmail(List<String> emails);
	
	Optional<List<String>> getUserWhoseBirthdayIsToday();
}