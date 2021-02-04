/**
 * 
 */
package com.stanzaliving.user.db.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.sqljpa.service.impl.AbstractJpaServiceImpl;
import com.stanzaliving.core.sqljpa.specification.utils.CriteriaOperation;
import com.stanzaliving.core.sqljpa.specification.utils.StanzaSpecificationBuilder;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.user.constants.UserQueryConstants;
import com.stanzaliving.user.db.service.UserDbService;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.repository.UserRepository;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Service
@Log4j2
public class UserDbServiceImpl extends AbstractJpaServiceImpl<UserEntity, Long, UserRepository>
		implements UserDbService {

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
	public Specification<UserEntity> getSearchQuery(UserFilterDto userFilterDto) {

		StanzaSpecificationBuilder<UserEntity> specificationBuilder = new StanzaSpecificationBuilder<>();

		if (CollectionUtils.isNotEmpty(userFilterDto.getUserIds())) {

			specificationBuilder.with(UserQueryConstants.UUID, CriteriaOperation.IN, userFilterDto.getUserIds());

		} else {

			if (StringUtils.isNotBlank(userFilterDto.getMobile())) {
				specificationBuilder.with(UserQueryConstants.MOBILE, CriteriaOperation.EQ, userFilterDto.getMobile());

				if (StringUtils.isNotBlank(userFilterDto.getIsoCode())) {
					specificationBuilder.with(UserQueryConstants.ISO_CODE, CriteriaOperation.EQ,
							userFilterDto.getIsoCode());
				}
			}

			if (StringUtils.isNotBlank(userFilterDto.getEmail())) {
				specificationBuilder.with(UserQueryConstants.EMAIL, CriteriaOperation.EQ, userFilterDto.getEmail());
			}

			if (Objects.nonNull(userFilterDto.getUserType())) {
				specificationBuilder.with(UserQueryConstants.USER_TYPE, CriteriaOperation.ENUM_EQ,
						userFilterDto.getUserType());
			}

			if (userFilterDto.getStatus() != null) {

				if (userFilterDto.getStatus()) {
					specificationBuilder.with(UserQueryConstants.STATUS, CriteriaOperation.TRUE, true);
				} else {
					specificationBuilder.with(UserQueryConstants.STATUS, CriteriaOperation.FALSE, false);
				}
			}

			if (Objects.nonNull(userFilterDto.getDepartment())) {
				specificationBuilder.with(UserQueryConstants.DEPARTMENT, CriteriaOperation.ENUM_EQ,
						userFilterDto.getDepartment());
			}

			if (StringUtils.isNotBlank(userFilterDto.getName())) {
				List<UserEntity> userEntities = userRepository.searchByName(userFilterDto.getName());
				if (CollectionUtils.isNotEmpty(userEntities)) {
					List<String> userIdList = new ArrayList<>();
					userEntities.forEach(userEntity -> {
						userIdList.add(userEntity.getUuid());
					});
					specificationBuilder.with(UserQueryConstants.UUID, CriteriaOperation.IN, userIdList);
				} else {
					specificationBuilder.with(UserQueryConstants.UUID, CriteriaOperation.EQ, -1);
				}
			}
		}

		return specificationBuilder.build();
	}

	@Override
	public List<UserEntity> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public UserEntity findByMobileAndUserType(String mobileNo,UserType userType) {

		return userRepository.findByMobileAndUserType(mobileNo,userType);
	}

	@Override
	public List<UserEntity> findByUserType(UserType userType) {
		
		log.info("CHecking fr user type {}",userType);
		
		List<UserEntity> userEntities =  userRepository.findAll();
		
		log.info("user size is {}",userEntities.size());
		
		return userEntities.stream().filter(dto->dto.getUserType().equals(userType)).collect(Collectors.toList());
	}

	@Override
	public UserEntity findByMobile(String mobile) {
		return userRepository.findByMobileAndIsoCode(mobile, "IN");
	}

	@Override
	public UserEntity findByUuidAndEmail(String userUuid, String email) {
		
		return getJpaRepository().findByUuidAndEmail(userUuid, email);
	}

}