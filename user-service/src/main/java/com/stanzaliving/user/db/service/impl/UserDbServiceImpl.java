/**
 * 
 */
package com.stanzaliving.user.db.service.impl;

import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.sqljpa.specification.utils.CriteriaOperation;
import com.stanzaliving.core.sqljpa.specification.utils.StanzaSpecificationBuilder;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.user.constants.UserQueryConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.sqljpa.service.impl.AbstractJpaServiceImpl;
import com.stanzaliving.user.db.service.UserDbService;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Service
public class UserDbServiceImpl extends AbstractJpaServiceImpl<UserEntity, Long, UserRepository> implements UserDbService {

	@Autowired
	private UserRepository userRepository;

	@Override
	protected UserRepository getJpaRepository() {
		return userRepository;
	}

	@Override
	public UserEntity getUserForMobile(String mobile, String isoCode) {
		return getJpaRepository().findByMobileAndIsoCode(mobile, isoCode);
	}

	@Override
	public Specification<UserEntity> getSearchQuery(List<String> userIds, String mobile, String isoCode, String email, UserType userType, Boolean status, Department department, String name) {

		StanzaSpecificationBuilder<UserEntity> specificationBuilder = new StanzaSpecificationBuilder<>();

		if (CollectionUtils.isNotEmpty(userIds)) {

			specificationBuilder.with(UserQueryConstants.UUID, CriteriaOperation.IN, userIds);

		} else {

			if (StringUtils.isNotBlank(mobile)) {
				specificationBuilder.with(UserQueryConstants.MOBILE, CriteriaOperation.EQ, mobile);

				if (StringUtils.isNotBlank(isoCode)) {
					specificationBuilder.with(UserQueryConstants.ISO_CODE, CriteriaOperation.EQ, isoCode);
				}
			}

			if (StringUtils.isNotBlank(email)) {
				specificationBuilder.with(UserQueryConstants.EMAIL, CriteriaOperation.EQ, email);
			}

			if (Objects.nonNull(userType)) {
				specificationBuilder.with(UserQueryConstants.USER_TYPE, CriteriaOperation.ENUM_EQ, userType);
			}

			if (status != null) {

				if (status) {
					specificationBuilder.with(UserQueryConstants.STATUS, CriteriaOperation.TRUE, true);
				} else {
					specificationBuilder.with(UserQueryConstants.STATUS, CriteriaOperation.FALSE, false);
				}
			}

			if(Objects.nonNull(department)){
				specificationBuilder.with(UserQueryConstants.DEPARTMENT, CriteriaOperation.ENUM_EQ, department);
			}

			if(StringUtils.isNotBlank(name)){
				List<UserEntity> userEntities = userRepository.searchByName(name);
				if(CollectionUtils.isNotEmpty(userEntities)){
					List<String> userIdList = new ArrayList<>();
					userEntities.forEach(userEntity -> {userIdList.add(userEntity.getUuid());});
					specificationBuilder.with(UserQueryConstants.UUID, CriteriaOperation.IN, userIdList);
				} else{
					specificationBuilder.with(UserQueryConstants.UUID, CriteriaOperation.EQ, -1);
				}
			}
		}

		return specificationBuilder.build();
	}

}