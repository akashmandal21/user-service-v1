/**
 * 
 */
package com.stanzaliving.user.db.service;

import org.springframework.data.jpa.domain.Specification;

import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.user.entity.UserEntity;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface UserDbService extends AbstractJpaService<UserEntity, Long> {

	UserEntity getUserForMobile(String mobile, String isoCode);
	
	UserEntity getUserForMobileAndUserType(String mobile, String isoCode,UserType userType);

	Specification<UserEntity> getSearchQuery(UserFilterDto userFilterDto);

}