/**
 * 
 */
package com.stanzaliving.user.db.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.stanzaliving.user.Projections.UserView;
import org.apache.commons.collections.CollectionUtils;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.Predicate;

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
	public UserEntity getUserForMobileNotMigrated(String mobile, String isoCode, boolean migrated) {
		return getJpaRepository().findByMobileAndIsoCodeAndMigrated(mobile, isoCode,migrated);
	}

//	@Override
//	public Specification<UserEntity> getSearchQuery(UserFilterDto userFilterDto) {
//
//		StanzaSpecificationBuilder<UserEntity> specificationBuilder = new StanzaSpecificationBuilder<>();
//
//		if (CollectionUtils.isNotEmpty(userFilterDto.getUserIds())) {
//
//			specificationBuilder.with(UserQueryConstants.UUID, CriteriaOperation.IN, userFilterDto.getUserIds());
//			if (userFilterDto.getStatus() != null) {
//				if (userFilterDto.getStatus()) {
//					specificationBuilder.with(UserQueryConstants.STATUS, CriteriaOperation.TRUE, true);
//				} else {
//					specificationBuilder.with(UserQueryConstants.STATUS, CriteriaOperation.FALSE, false);
//				}
//			}
//
//		} else {
//
//			if (StringUtils.isNotBlank(userFilterDto.getMobile())) {
//				specificationBuilder.with(UserQueryConstants.MOBILE, CriteriaOperation.EQ, userFilterDto.getMobile());
//
//				if (StringUtils.isNotBlank(userFilterDto.getIsoCode())) {
//					specificationBuilder.with(UserQueryConstants.ISO_CODE, CriteriaOperation.EQ,
//							userFilterDto.getIsoCode());
//				}
//			}
//
//			if (StringUtils.isNotBlank(userFilterDto.getEmail())) {
//				specificationBuilder.with(UserQueryConstants.EMAIL, CriteriaOperation.EQ, userFilterDto.getEmail());
//			}
//
//			if (Objects.nonNull(userFilterDto.getUserType())) {
//				specificationBuilder.with(UserQueryConstants.USER_TYPE, CriteriaOperation.ENUM_EQ,
//						userFilterDto.getUserType());
//			}
//
//			if(userFilterDto.getMigrated()!=null){
//				if(userFilterDto.getMigrated()) {
//					specificationBuilder.with("migrated", CriteriaOperation.TRUE, Boolean.TRUE);
//				}
//				else{
//					specificationBuilder.with("migrated",CriteriaOperation.FALSE,Boolean.FALSE);
//				}
//			}
//
//			if (userFilterDto.getStatus() != null) {
//
//				if (userFilterDto.getStatus()) {
//					specificationBuilder.with(UserQueryConstants.STATUS, CriteriaOperation.TRUE, true);
//				} else {
//					specificationBuilder.with(UserQueryConstants.STATUS, CriteriaOperation.FALSE, false);
//				}
//			}
//
//			if (Objects.nonNull(userFilterDto.getDepartment())) {
//				specificationBuilder.with(UserQueryConstants.DEPARTMENT, CriteriaOperation.ENUM_EQ,
//						userFilterDto.getDepartment());
//			}
//
//			if (StringUtils.isNotBlank(userFilterDto.getName())) {
//				if (StringUtils.isNotBlank(userFilterDto.getName())) {
//					specificationBuilder.with(UserQueryConstants.FIRST_NAME, CriteriaOperation.LIKE, "%" + userFilterDto.getName() + "%");
//					specificationBuilder.with(UserQueryConstants.LAST_NAME, CriteriaOperation.LIKE, "%" + userFilterDto.getName() + "%");
//				}
//				List<UserEntity> userEntities = userRepository.searchByName(userFilterDto.getName());
//				if (CollectionUtils.isNotEmpty(userEntities)) {
//					List<String> userIdList = new ArrayList<>();
//					userEntities.forEach(userEntity -> {
//						userIdList.add(userEntity.getUuid());
//					});
//					specificationBuilder.with(UserQueryConstants.UUID, CriteriaOperation.IN, userIdList);
//				} else {
//					specificationBuilder.with(UserQueryConstants.UUID, CriteriaOperation.EQ, -1);
//				}
//			}
//		}
//
//		return specificationBuilder.build();
//	}

	@Override
	public Specification<UserEntity> getSearchQuery(UserFilterDto userFilterDto) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (StringUtils.hasText(userFilterDto.getName())) {
				Predicate firstNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(UserQueryConstants.FIRST_NAME)),
						"%" + userFilterDto.getName().toLowerCase() + "%");
				Predicate lastNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(UserQueryConstants.LAST_NAME)),
						"%" + userFilterDto.getName().toLowerCase() + "%");
				predicates.add(firstNamePredicate);
				predicates.add(lastNamePredicate);
			}

			if (org.springframework.util.StringUtils.hasText(userFilterDto.getMobile())) {
				Predicate mobilePredicate = criteriaBuilder.equal(root.get(UserQueryConstants.MOBILE),
						userFilterDto.getMobile());
				predicates.add(mobilePredicate);

				if (org.springframework.util.StringUtils.hasText(userFilterDto.getIsoCode())) {
					Predicate isoCodePredicate = criteriaBuilder.equal(root.get(UserQueryConstants.ISO_CODE),
							userFilterDto.getIsoCode());
					predicates.add(isoCodePredicate);
				}
			}

			if (StringUtils.hasText(userFilterDto.getEmail())) {
				Predicate emailPredicate = criteriaBuilder.equal(root.get(UserQueryConstants.EMAIL),
						userFilterDto.getEmail());
				predicates.add(emailPredicate);
			}

			if (userFilterDto.getUserType() != null) {
				Predicate userTypePredicate = criteriaBuilder.equal(root.get(UserQueryConstants.USER_TYPE),
						userFilterDto.getUserType());
				predicates.add(userTypePredicate);
			}

			if (userFilterDto.getMigrated() != null) {
				Predicate migratedPredicate = criteriaBuilder.equal(root.get("migrated"), userFilterDto.getMigrated());
				predicates.add(migratedPredicate);
			}

			if (userFilterDto.getStatus() != null) {
				Predicate statusPredicate = criteriaBuilder.equal(root.get(UserQueryConstants.STATUS),
						userFilterDto.getStatus());
				predicates.add(statusPredicate);
			}

			if (userFilterDto.getDepartment() != null) {
				Predicate departmentPredicate = criteriaBuilder.equal(root.get(UserQueryConstants.DEPARTMENT),
						userFilterDto.getDepartment());
				predicates.add(departmentPredicate);
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
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
	public List<UserEntity> findByMobileIn(Set<String> mobileNos) {
		
		return getJpaRepository().findByMobileIn(mobileNos);
	}
	

	public UserEntity findByMobile(String mobile) {
		return userRepository.findByMobileAndIsoCode(mobile, "IN");
	}

	@Override
	public UserEntity findByMobileNotMigrated(String mobile, boolean migrated) {
		return userRepository.findByMobileAndIsoCodeAndMigrated(mobile, "IN",migrated);
	}

	@Override
	public UserEntity findByUuidNotMigrated(String uuid, boolean migrated) {
		return getJpaRepository().findByUuidAndMigrated(uuid,migrated);
	}

	@Override
	public UserEntity findByUuidAndEmail(String userUuid, String email) {
		
		return getJpaRepository().findByUuidAndEmail(userUuid, email);
	}

	@Override
	public UserEntity findTop1ByEmailOrderByCreatedAtDesc(String email) {
		
		return getJpaRepository().findTop1ByEmailOrderByCreatedAtDesc(email);
	}

	@Override
	public Map<String, String> getUuidByEmail(List<String> emails) {

		List<UserView> viewList = getJpaRepository().findByEmailInAndStatus(emails, true);
		Map<String,String > response = new HashMap<>();
		viewList.forEach(userView -> response.put(userView.getEmail(), userView.getUuid()));
		return response;
	}

	@Override
	public UserEntity findActiveUserByEmail(String emailId) {
		UserEntity user = getJpaRepository().findByEmailAndStatus(emailId,true);
		return user;
	}

	@Override
	public Optional<List<String>> getUserWhoseBirthdayIsToday() {
		return getJpaRepository().findUsersWhoseBirthdayIsToday();
	}

	@Override
	public List<UserEntity> findAllByUuidInAndStatus(List<String> userId, boolean b) {
		return getJpaRepository().findByUuidInAndStatus(userId, b);
	}

	@Override
	public List<UserEntity> findByUserTypeIn(List<UserType> asList) {
		return getJpaRepository().findByUserTypeIn(asList);
	}

	@Override
	public List<UserEntity> findByUserTypeInAndStatus(List<UserType> asList,boolean status) {
		return getJpaRepository().findByUserTypeInAndStatus(asList,status);
	}

	@Override
	public List<UserEntity> findByUuidInNotMigrated(List<String> userUuids, boolean migrated) {
		return getJpaRepository().findByUuidInAndMigrated(userUuids,migrated);
	}

	@Override
	public List<UserEntity> findByEmailNotMigrated(String email, boolean migrated) {
		return  findByEmailAndMigrated(email,migrated);
	}

	private List<UserEntity> findByEmailAndMigrated(String email, boolean migrated) {
		return getJpaRepository().findByEmailAndMigrated(email,migrated);
	}

}