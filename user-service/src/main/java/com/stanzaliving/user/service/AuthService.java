/**
 * 
 */
package com.stanzaliving.user.service;

import javax.validation.Valid;

import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.request.dto.EmailOtpValidateRequestDto;
import com.stanzaliving.core.user.request.dto.EmailVerificationRequestDto;
import com.stanzaliving.core.user.request.dto.LoginRequestDto;
import com.stanzaliving.core.user.request.dto.OtpValidateRequestDto;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface AuthService {

	void login(LoginRequestDto loginRequestDto);

	UserProfileDto validateOtp(OtpValidateRequestDto otpValidateRequestDto);

	void resendOtp(LoginRequestDto loginRequestDto);

	void sendEmailOtp(EmailVerificationRequestDto emailVerificationRequestDto);

	UserProfileDto validateEmailVerificationOtp(@Valid EmailOtpValidateRequestDto emailOtpValidateRequestDto);

	void resendEmailOtp(@Valid EmailVerificationRequestDto emailVerificationRequestDto);

}