/**
 * 
 */
package com.stanzaliving.user.service;

import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.request.dto.LoginRequestDto;
import com.stanzaliving.core.user.request.dto.OtpValidateRequestDto;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface AuthService {

	void login(LoginRequestDto loginRequestDto);

	UserDto validateOtp(OtpValidateRequestDto otpValidateRequestDto);

	void resendOtp(LoginRequestDto loginRequestDto);

}