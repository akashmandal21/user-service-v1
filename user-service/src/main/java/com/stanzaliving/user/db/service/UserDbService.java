/**
 * 
 */
package com.stanzaliving.user.db.service;

import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface UserDbService extends AbstractJpaService<UserEntity, Long> {

	UserEntity getUserForMobile(String mobile, String isoCode);

	Specification<UserEntity> getSearchQuery(UserFilterDto userFilterDto);

	Page<UserEntity> findByUuids(List<String> userUuids, int pageNo, int limit);

	List<UserEntity> findByUuids(List<String> userUuids);
}