/**
 * 
 */
package com.stanzaliving.user.service.impl;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.base.utils.PhoneNumberUtils;
import com.stanzaliving.core.sqljpa.specification.utils.CriteriaOperation;
import com.stanzaliving.core.sqljpa.specification.utils.StanzaSpecificationBuilder;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.user.adapters.UserAdapter;
import com.stanzaliving.user.constants.UserQueryConstants;
import com.stanzaliving.user.db.service.UserDbService;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.entity.UserProfileEntity;
import com.stanzaliving.user.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Log4j2
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDbService userDbService;

	@Override
	public UserProfileDto getActiveUserByUserId(String userId) {

		log.info("Searching User by UserId: " + userId);

		UserEntity userEntity = userDbService.findByUuidAndStatus(userId, true);

		if (Objects.isNull(userEntity)) {
			throw new StanzaException("User not found for UserId: " + userId);
		}

		return UserAdapter.getUserProfileDto(userEntity);
	}

	@Override
	public void assertActiveUserByUserUuid(String userUuid) {
		this.getActiveUserByUserId(userUuid);
	}

	@Override
	public UserDto addUser(AddUserRequestDto addUserRequestDto) {

		if (!PhoneNumberUtils.isValidMobileForCountry(addUserRequestDto.getMobile(), addUserRequestDto.getIsoCode())) {
			log.error("Number: " + addUserRequestDto.getMobile() + " and ISO: " + addUserRequestDto.getIsoCode() + " doesn't appear to be a valid mobile combination");
			throw new StanzaException("Mobile Number and ISO Code combination not valid");
		}

		UserEntity userEntity =
				userDbService.getUserForMobile(addUserRequestDto.getMobile(), addUserRequestDto.getIsoCode());

		if (Objects.nonNull(userEntity)) {
			log.warn("User: " + userEntity.getUuid() + " already exists for Mobile: " + addUserRequestDto.getMobile() + ", ISO Code: " + addUserRequestDto.getIsoCode() + " of type: "
					+ addUserRequestDto.getUserType());
			throw new StanzaException("User already exists with mobile");
		}

		log.info("Adding new User [Mobile: " + addUserRequestDto.getMobile() + ", ISOCode: " + addUserRequestDto.getIsoCode() + ", UserType: " + addUserRequestDto.getUserType() + "]");

		UserProfileEntity profileEntity = UserAdapter.getUserProfileEntity(addUserRequestDto);

		userEntity =
				UserEntity.builder()
						.userType(addUserRequestDto.getUserType())
						.isoCode(addUserRequestDto.getIsoCode())
						.mobile(addUserRequestDto.getMobile())
						.mobileVerified(false)
						.email(addUserRequestDto.getEmail())
						.emailVerified(false)
						.userProfile(profileEntity)
						.status(true)
						.department(addUserRequestDto.getDepartment())
						.build();

		profileEntity.setUser(userEntity);

		userEntity = userDbService.saveAndFlush(userEntity);

		log.info("Added New User with Id: " + userEntity.getUuid());

		return UserAdapter.getUserDto(userEntity);
	}

	@Override
	public UserProfileDto getUserProfile(String userId) {

		UserEntity userEntity = userDbService.findByUuid(userId);

		if (Objects.isNull(userEntity)) {
			throw new StanzaException("User not found for UserId: " + userId);
		}

		return UserAdapter.getUserProfileDto(userEntity);
	}

	@Override
	public PageResponse<UserProfileDto> searchUser(List<String> userIds, String mobile, String isoCode, String email, UserType userType, Boolean status, int pageNo, int limit) {

		Page<UserEntity> userPage = getUserPage(userIds, mobile, isoCode, email, userType, status, pageNo, limit);

		log.info("Found " + userPage.getNumberOfElements() + " User Records on Page: " + pageNo + " for Search Criteria");

		List<UserProfileDto> userDtos = userPage.getContent().stream().map(UserAdapter::getUserProfileDto).collect(Collectors.toList());

		return new PageResponse<>(pageNo, userPage.getNumberOfElements(), userPage.getTotalPages(), userPage.getTotalElements(), userDtos);

	}

	private Page<UserEntity> getUserPage(List<String> userIds, String mobile, String isoCode, String email, UserType userType, Boolean status, int pageNo, int limit) {

		Specification<UserEntity> specification = getSearchQuery(userIds, mobile, isoCode, email, userType, status);

		Pageable pagination = getPaginationForSearchRequest(pageNo, limit);

		return userDbService.findAll(specification, pagination);
	}

	private Specification<UserEntity> getSearchQuery(List<String> userIds, String mobile, String isoCode, String email, UserType userType, Boolean status) {

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
		}

		return specificationBuilder.build();
	}

	private Pageable getPaginationForSearchRequest(int pageNo, int limit) {

		Pageable pagination = PageRequest.of(0, 10, Direction.DESC, "createdAt");

		if (pageNo > 0 && limit > 0 && limit < 1000) {
			pagination = PageRequest.of(pageNo - 1, limit, Direction.DESC, "createdAt");
		}

		return pagination;
	}

}