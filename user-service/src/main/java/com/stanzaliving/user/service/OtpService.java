/**
 * 
 */
package com.stanzaliving.user.service;

import javax.validation.Valid;

import com.stanzaliving.core.user.enums.OtpType;
import com.stanzaliving.core.user.request.dto.EmailOtpValidateRequestDto;
import com.stanzaliving.core.user.request.dto.EmailVerificationRequestDto;
import com.stanzaliving.core.user.request.dto.LoginRequestDto;
import com.stanzaliving.core.user.request.dto.MobileEmailOtpRequestDto;
import com.stanzaliving.core.user.request.dto.MobileOtpRequestDto;
import com.stanzaliving.core.user.request.dto.OtpValidateRequestDto;
import com.stanzaliving.user.entity.UserEntity;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface OtpService {

	void sendLoginOtp(UserEntity userEntity);

	void validateLoginOtp(OtpValidateRequestDto otpValidateRequestDto);

	void validateMobileOtp(String mobile, String isoCode, String otp, OtpType otpType);

	void resendLoginOtp(LoginRequestDto loginRequestDto);

	void resendMobileOtp(String mobile, String isoCode, OtpType otpType);

	void sendMobileOtp(MobileOtpRequestDto mobileOtpRequestDto);

	void sendOtp(MobileEmailOtpRequestDto mobileEmailOtpRequestDto);

	void sendEmailOtp(UserEntity userEntity);

	void validateEmailVerificationOtp(EmailOtpValidateRequestDto emailOtpValidateRequestDto);

	void resendEmailVerificationOtp(@Valid EmailVerificationRequestDto emailVerificationRequestDto);
}