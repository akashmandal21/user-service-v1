/**
 * 
 */
package com.stanzaliving.user.controller;

import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.constants.SecurityConstants;
import com.stanzaliving.core.base.utils.SecureCookieUtil;
import com.stanzaliving.core.base.utils.StanzaUtils;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.request.dto.LoginRequestDto;
import com.stanzaliving.core.user.request.dto.OtpValidateRequestDto;
import com.stanzaliving.user.entity.UserSessionEntity;
import com.stanzaliving.user.service.AuthService;
import com.stanzaliving.user.service.SessionService;

import lombok.extern.log4j.Log4j;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Log4j
@RestController
@RequestMapping("auth")
public class AuthController {

	@Autowired
	private AuthService authService;

	@Autowired
	private SessionService sessionService;

	@PostMapping("login")
	public ResponseDto<Void> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {

		authService.login(loginRequestDto);

		return ResponseDto.success("OTP Sent for Login");
	}

	@PostMapping("validateOtp")
	public ResponseDto<UserDto> validateOtp(
			@RequestBody @Valid OtpValidateRequestDto otpValidateRequestDto, HttpServletRequest request, HttpServletResponse response) {

		UserDto userDto = authService.validateOtp(otpValidateRequestDto);

		log.info("OTP Successfully Validated for User: " + userDto.getUuid() + ". Creating User Session now");

		String token = StanzaUtils.generateUniqueId();

		UserSessionEntity userSessionEntity = sessionService.createUserSession(userDto, token);

		if (Objects.nonNull(userSessionEntity)) {
			addTokenToResponse(request, response, token);

			return ResponseDto.success("User Login Successfull", userDto);
		}

		return ResponseDto.failure("Failed to create user session");
	}

	@PostMapping("resendOtp")
	public ResponseDto<Void> resendOtp(@RequestBody @Valid LoginRequestDto loginRequestDto) {

		authService.resendOtp(loginRequestDto);

		return ResponseDto.success("OTP Successfully Resent");
	}

	@GetMapping("logout")
	public ResponseDto<Void> logout(
			@CookieValue(name = SecurityConstants.TOKEN_HEADER_NAME) String token,
			HttpServletRequest request, HttpServletResponse response) {

		String userId = request.getParameter(SecurityConstants.USER_ID);

		log.info("Logout requested for user: " + userId);

		sessionService.removeUserSession(token);

		SecureCookieUtil.handleLogOutResponse(request, response);

		return ResponseDto.success("Successfully Logged Out");
	}

	private void addTokenToResponse(HttpServletRequest request, HttpServletResponse response, String token) {

		if (StringUtils.isNotBlank(token)) {

			String frontEnv = request.getHeader(SecurityConstants.FRONT_ENVIRONMENT);
			boolean isLocalFrontEnd = StringUtils.isNotBlank(frontEnv) && SecurityConstants.FRONT_ENVIRONMENT_LOCAL.equals(frontEnv);

			String appEnv = request.getHeader(SecurityConstants.APP_ENVIRONMENT);
			boolean isApp = StringUtils.isNotBlank(appEnv) && SecurityConstants.APP_ENVIRONMENT_TRUE.equals(appEnv);

			response.addCookie(SecureCookieUtil.create(SecurityConstants.TOKEN_HEADER_NAME, token, Optional.of(isLocalFrontEnd), Optional.of(isApp)));
		}
	}
}