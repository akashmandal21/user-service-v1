/**
 * 
 */
package com.stanzaliving.user.service;

import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.request.dto.EmailOtpValidateRequestDto;
import com.stanzaliving.core.user.request.dto.EmailVerificationRequestDto;
import com.stanzaliving.core.user.request.dto.LoginRequestDto;
import com.stanzaliving.core.user.request.dto.OtpValidateRequestDto;
import com.stanzaliving.user.dto.request.LoginDto;
import com.stanzaliving.user.dto.request.OtpRequestDto;
import com.stanzaliving.user.entity.UserEntity;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface AuthService {

	void login(LoginRequestDto loginRequestDto);

	void loginViaEmployeeCode(LoginDto loginDto);

	UserProfileDto validateOtp(OtpValidateRequestDto otpValidateRequestDto);

	UserProfileDto validateOtpForEmployeeCode(OtpRequestDto otpRequestDto);

	void resendOtp(LoginRequestDto loginRequestDto);

	void sendEmailOtp(EmailVerificationRequestDto emailVerificationRequestDto);

	UserEntity validateEmailVerificationOtpAndUpdateUserDetails(EmailOtpValidateRequestDto emailOtpValidateRequestDto);

	void resendEmailOtp(EmailVerificationRequestDto emailVerificationRequestDto);

	UserProfileDto loginWithTrueCaller(LoginRequestDto loginRequestDto);
}