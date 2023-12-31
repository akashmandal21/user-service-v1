/**
 *
 */
package com.stanzaliving.user.controller;

import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.base.exception.UserValidationException;
import com.stanzaliving.core.user.enums.App;
import com.stanzaliving.core.user.enums.OtpType;
import com.stanzaliving.user.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import com.stanzaliving.booking.dto.BookingResponseDto;
import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.constants.SecurityConstants;
import com.stanzaliving.core.base.utils.ObjectMapperUtil;
import com.stanzaliving.core.base.utils.SecureCookieUtil;
import com.stanzaliving.core.base.utils.StanzaUtils;
import com.stanzaliving.core.user.acl.dto.AclUserDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.core.user.request.dto.EmailOtpValidateRequestDto;
import com.stanzaliving.core.user.request.dto.EmailVerificationRequestDto;
import com.stanzaliving.core.user.request.dto.LoginRequestDto;
import com.stanzaliving.core.user.request.dto.OtpValidateRequestDto;
import com.stanzaliving.user.acl.service.AclService;
import com.stanzaliving.user.adapters.UserAdapter;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.entity.UserSessionEntity;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Log4j2
@RestController
@RequestMapping("auth")
public class AuthController {

	@Autowired
	@Lazy
	private AuthService authService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private UserService userService;

	@Autowired
	private AclService aclService;

	@Autowired
	private OnboardGuestService onboardGuestService;

	@Autowired
	private OtpService otpService;

	@PostMapping("login")
	public ResponseDto<Void> login(@RequestBody @Valid LoginRequestDto loginRequestDto, HttpServletRequest request, HttpServletResponse response) {

		authService.login(loginRequestDto);

		SecureCookieUtil.handleLogOutResponse(request, response);

		return ResponseDto.success("OTP Sent for Login");
	}

	@PostMapping("validateOtp")
	public ResponseDto<AclUserDto> validateOtp(
			@RequestBody @Valid OtpValidateRequestDto otpValidateRequestDto, HttpServletRequest request, HttpServletResponse response, @RequestHeader(name = "app", required = false) App app, @RequestHeader(name = "deviceId", required = false) String deviceId) {

		UserProfileDto userProfileDto = authService.validateOtp(otpValidateRequestDto);

		log.info("OTP Successfully Validated for User: " + userProfileDto.getUuid() + ". Creating User Session now " + userProfileDto.getUserType());

		String token = StanzaUtils.generateUniqueId();

		log.debug("app : {}, deviceId : {}", app, deviceId);

		UserSessionEntity userSessionEntity = sessionService.createUserSession(userProfileDto, token, app, deviceId);

		if (Objects.nonNull(userSessionEntity)) {
			sessionService.validatePreviousSessions(userProfileDto.getUuid(), app, deviceId);
			addTokenToResponse(request, response, token, userSessionEntity);
			if(UserType.INVITED_GUEST.equals(userProfileDto.getUserType())) {

				log.info("UserType for user is INVITED_GUEST {} " + userProfileDto.getUuid());
				ResponseDto<BookingResponseDto> bookingResponseDto = onboardGuestService.createGuestBooking(userProfileDto.getMobile());

				log.info("\n\n\n\n\n OTP Successfully bookingResponseDto " + bookingResponseDto);
				if (Objects.isNull(bookingResponseDto) ) {
					return ResponseDto.failure("Failed to create guest booking for " + userProfileDto.getMobile());
				}

			}

			return ResponseDto.success("User Login Successfull", UserAdapter.getAclUserDto(userProfileDto, aclService.getUserDeptLevelRoleNameUrlExpandedDtoFe(userProfileDto.getUuid())));
		}

		return ResponseDto.failure("Failed to create user session");
	}

	@GetMapping("refresh")
	public ResponseDto<AclUserDto> refreshSession(
			@CookieValue(name = SecurityConstants.TOKEN_HEADER_NAME) String token, HttpServletRequest request, HttpServletResponse response,
			@RequestHeader(name = "app", required = false) App app, @RequestHeader(name = "deviceId", required = false) String deviceId) {

		UserSessionEntity userSessionEntity = sessionService.refreshUserSession(token, app, deviceId);

		log.debug("app : {}, deviceId : {}", app, deviceId);

		if (Objects.nonNull(userSessionEntity)) {
			log.info("Successfully refreshed userSessionEntity for user : {} . Adding token to response ...",
					userSessionEntity.getUuid());
			try {
				sessionService.validatePreviousSessions(userSessionEntity.getUuid(), app, deviceId);
			} catch (StanzaException se) {
				throw new UserValidationException(se.getMessage());
			}
			addTokenToResponse(request, response, userSessionEntity.getToken(), userSessionEntity);
			ResponseDto<AclUserDto> aclUserDtoResponseDto = ResponseDto.success("Token refreshed Successfully",
					UserAdapter.getAclUserDto(userService.getUserProfile(userSessionEntity.getUserId()),
							aclService.getUserDeptLevelRoleNameUrlExpandedDtoFe(userSessionEntity.getUserId())));
			log.info("Successfully refreshSession for user : {}", userSessionEntity.getUserId());
			return aclUserDtoResponseDto;
		}

		log.error("Cannot refreshSession. userSessionEntity is null");
		return ResponseDto.failure("Failed to refresh user session");
	}

	@PostMapping("sendEmailVerificationOtp")
	public ResponseDto<Void> sendEmailVerificationOtp(@RequestBody @Valid EmailVerificationRequestDto emailVerificationRequestDto) {

		log.info("Request received to send otp for email verification: {}", emailVerificationRequestDto);

		authService.sendEmailOtp(emailVerificationRequestDto);

		return ResponseDto.success("Email OTP Sent");
	}

	@PostMapping("resendEmailVerificationOtp")
	public ResponseDto<Void> resendEmailVerificationOtp(@RequestBody @Valid EmailVerificationRequestDto emailVerificationRequestDto) {

		log.info("Request received to re-send otp for email verification: {}", emailVerificationRequestDto);

		authService.resendEmailOtp(emailVerificationRequestDto);

		return ResponseDto.success("OTP Successfully Resent");
	}

	@PostMapping("validateEmailVerificationOtp")
	public ResponseDto<String> validateEmailVerificationOtp(@RequestBody EmailOtpValidateRequestDto emailOtpValidateRequestDto) {

		log.info("Request received to validate otp for email verification and update Deatils: {}", ObjectMapperUtil.getString(emailOtpValidateRequestDto));

		UserEntity userEntity = authService.validateEmailVerificationOtpAndUpdateUserDetails(emailOtpValidateRequestDto);

		log.info("Email OTP Successfully verified for User: " + userEntity.getUuid());

		return ResponseDto.success("Email OTP Successfully verified for User: " + userEntity.getUuid() + " with Email: " + userEntity.getEmail() + " and User Details Updated Successfully.");
	}

	@PostMapping("resendOtp")
	public ResponseDto<Void> resendOtp(@RequestBody @Valid LoginRequestDto loginRequestDto) {

		authService.resendOtp(loginRequestDto);

		return ResponseDto.success("OTP Successfully Resent");
	}

	@GetMapping("logout")
	public ResponseDto<Void> logout(
			@CookieValue(name = SecurityConstants.TOKEN_HEADER_NAME, required = false, defaultValue = "default") String token,
			HttpServletRequest request, HttpServletResponse response) {

		String userId = request.getParameter(SecurityConstants.USER_ID);

		log.info("Logout requested for user: " + userId);

		if(!token.equalsIgnoreCase("default"))
			sessionService.removeUserSession(token);

		SecureCookieUtil.handleLogOutResponse(request, response);

		return ResponseDto.success("Successfully Logged Out");
	}

	@PostMapping("logout")
	public ResponseDto<Void> userLogout(
			@CookieValue(name = SecurityConstants.TOKEN_HEADER_NAME, required = false, defaultValue = "default") String token,
			HttpServletRequest request, HttpServletResponse response) {

		String userId = request.getParameter(SecurityConstants.USER_ID);

		log.info("Logout requested for user: " + userId);

		if(!token.equalsIgnoreCase("default"))
			sessionService.removeUserSession(token);

		SecureCookieUtil.handleLogOutResponse(request, response);

		return ResponseDto.success("Successfully Logged Out");
	}

	private void addTokenToResponse(HttpServletRequest request, HttpServletResponse response, String token,
									UserSessionEntity userSessionEntity) {

		try {
			log.info("Request received for addTokenToResponse for user : {}", userSessionEntity.getUserId());

			if (StringUtils.isNotBlank(token)) {

				String frontEnv = request.getHeader(SecurityConstants.FRONT_ENVIRONMENT);
				boolean isLocalFrontEnd = StringUtils.isNotBlank(frontEnv) && SecurityConstants.FRONT_ENVIRONMENT_LOCAL.equals(frontEnv);

				String appEnv = request.getHeader(SecurityConstants.APP_ENVIRONMENT);
				boolean isApp = StringUtils.isNotBlank(appEnv) && SecurityConstants.APP_ENVIRONMENT_TRUE.equals(appEnv);
				String domainName = request.getHeader("origin");
				if(StringUtils.isNotBlank(domainName)) {
					if(domainName.trim().contains("://"))
						domainName = domainName.substring(domainName.indexOf("://") + 3);
				}
				log.info("domainName {}", domainName);
				response.addCookie(SecureCookieUtil.create(SecurityConstants.TOKEN_HEADER_NAME, token, Optional.of(isLocalFrontEnd), Optional.of(isApp), domainName));
				log.info("Successfully added token to response for user : {}", userSessionEntity.getUserId());
			} else {
				log.warn("Cannot add token to response, token not present for user :{}", userSessionEntity.getUserId());
			}
		} catch (Exception e) {
			log.error("Exception while addTokenToResponse for user : {} , exception : {}",
					userSessionEntity.getUserId(), e.getMessage());
			throw new StanzaException(e);
		}
	}

	@PostMapping("login/trueCaller")
	public ResponseDto<AclUserDto> loginWithTrueCaller(
			@RequestBody @Valid LoginRequestDto loginRequestDto,
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestHeader(name = "app", required = false) App app, @RequestHeader(name = "deviceId", required = false) String deviceId) {

		UserProfileDto userProfileDto = authService.loginWithTrueCaller(loginRequestDto);

		log.info("OTP Successfully Validated for User: " + userProfileDto.getUuid() + ". Creating User Session now " + userProfileDto.getUserType());

		String token = StanzaUtils.generateUniqueId();

		log.debug("app : {}, deviceId : {}", app, deviceId);

		UserSessionEntity userSessionEntity = sessionService.createUserSession(userProfileDto, token, app, deviceId);

		if (Objects.nonNull(userSessionEntity)) {
			sessionService.validatePreviousSessions(userProfileDto.getUuid(), app, deviceId);
			addTokenToResponse(request, response, token, userSessionEntity);
			if(UserType.INVITED_GUEST.equals(userProfileDto.getUserType())) {

				log.info("UserType for user is INVITED_GUEST {} " + userProfileDto.getUuid());
				ResponseDto<BookingResponseDto> bookingResponseDto = onboardGuestService.createGuestBooking(userProfileDto.getMobile());

				log.info("\n\n\n\n\n OTP Successfully bookingResponseDto " + bookingResponseDto);
				if (Objects.isNull(bookingResponseDto) ) {
					return ResponseDto.failure("Failed to create guest booking for " + userProfileDto.getMobile());
				}

			}

			return ResponseDto.success("User Login with TrueCaller Successful", UserAdapter.getAclUserDto(userProfileDto, aclService.getUserDeptLevelRoleNameUrlExpandedDtoFe(userProfileDto.getUuid())));
		}

		return ResponseDto.failure("Failed to create user session");
	}

	@GetMapping("getotp")
	public ResponseDto<Integer> getOtp(@RequestParam(value = "mobile", required = true) String mobile,
									   @RequestParam(value = "isoCode", required = true, defaultValue = "IN") String isoCode,
									   @RequestParam(value = "otpType", defaultValue = "LOGIN") OtpType otpType) {

		return ResponseDto.success("OTP is", otpService.getOtp(mobile, isoCode, otpType));
	}
}