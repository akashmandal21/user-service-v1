/**
 * 
 */
package com.stanzaliving.user.db.service;

import org.springframework.data.jpa.domain.Specification;

import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.user.entity.UserEntity;

import java.util.Collection;
import java.util.List;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface UserDbService extends AbstractJpaService<UserEntity, Long> {

	UserEntity getUserForMobile(String mobile, String isoCode);

	Specification<UserEntity> getSearchQuery(UserFilterDto userFilterDto);

	List<UserEntity> findByEmailInAndStatus(Collection<String> emails, boolean status);
}