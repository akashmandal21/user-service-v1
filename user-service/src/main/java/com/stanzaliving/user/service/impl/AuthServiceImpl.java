/**
 * 
 */
package com.stanzaliving.user.service.impl;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.constants.UserErrorCodes;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.core.user.request.dto.LoginRequestDto;
import com.stanzaliving.core.user.request.dto.OtpValidateRequestDto;
import com.stanzaliving.user.adapters.UserAdapter;
import com.stanzaliving.user.db.service.UserDbService;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.entity.UserProfileEntity;
import com.stanzaliving.user.exception.AuthException;
import com.stanzaliving.user.service.AuthService;
import com.stanzaliving.user.service.OtpService;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Log4j2
@Service
public class AuthServiceImpl implements AuthService {

	@Autowired
	private OtpService otpService;

	@Autowired
	private UserDbService userDbService;

	@Override
	public void login(LoginRequestDto loginRequestDto) {

		UserEntity userEntity = getActiveUser(loginRequestDto);

		otpService.sendLoginOtp(userEntity);

		log.info("OTP sent for User: " + userEntity.getUuid() + " for Login");

	}

	private UserEntity getActiveUser(LoginRequestDto loginRequestDto) {

		UserEntity userEntity =null;
		
		if(Objects.nonNull(loginRequestDto.getUserType()) && loginRequestDto.getUserType().equals(UserType.CONSUMER)) {
			userEntity = userDbService.getUserForMobileAndUserType(loginRequestDto.getMobile(), loginRequestDto.getIsoCode(),loginRequestDto.getUserType());
		}else {
			userEntity = userDbService.getUserForMobile(loginRequestDto.getMobile(), loginRequestDto.getIsoCode());
		}

		//userEntity = createUserIfUserIsConsumer(loginRequestDto, userEntity);
		
		if (Objects.isNull(userEntity)) {
			throw new AuthException("User Not Found For Login", UserErrorCodes.USER_NOT_EXISTS);
		}

		if (!userEntity.isStatus()) {
			throw new AuthException("User Account is Disabled", UserErrorCodes.USER_ACCOUNT_INACTIVE);
		}

		log.info("Found User: " + userEntity.getUuid() + " for Mobile: " + loginRequestDto.getMobile() + " of Type: " + userEntity.getUserType());

		return userEntity;
	}

	private UserEntity createUserIfUserIsConsumer(LoginRequestDto loginRequestDto, UserEntity userEntity) {
		
		if(Objects.isNull(userEntity) 
				&& Objects.nonNull(loginRequestDto.getUserType()) 
				&& UserType.CONSUMER == loginRequestDto.getUserType()) {
			
			UserProfileEntity userProfileEntity =
					UserProfileEntity
							.builder()
							.firstName("")
							.build();
			
			userEntity = UserEntity.builder()
					.isoCode(loginRequestDto.getIsoCode())
					.mobile(loginRequestDto.getMobile())
					.userType(loginRequestDto.getUserType())
					.department(Department.WEB)
					.userProfile(userProfileEntity)
					.build();
			
			userProfileEntity.setUser(userEntity);
			
			userEntity = userDbService.save(userEntity);
		}
		
		return userEntity;
	}

	@Override
	public UserProfileDto validateOtp(OtpValidateRequestDto otpValidateRequestDto) {

		UserEntity userEntity = getActiveUser(otpValidateRequestDto);

		otpService.validateLoginOtp(otpValidateRequestDto);

		log.info("OTP verification completed for User: " + userEntity.getUuid());

		userEntity.setMobileVerified(true);

		userDbService.update(userEntity);

		return UserAdapter.getUserProfileDto(userEntity);
	}

	@Override
	public void resendOtp(LoginRequestDto loginRequestDto) {

		otpService.resendLoginOtp(loginRequestDto);
	}

}