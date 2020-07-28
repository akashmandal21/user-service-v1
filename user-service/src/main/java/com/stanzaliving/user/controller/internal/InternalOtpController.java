package com.stanzaliving.user.controller.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.user.request.dto.MobileOtpRequestDto;
import com.stanzaliving.core.user.request.dto.MobileOtpValidateRequestDto;
import com.stanzaliving.user.exception.AuthException;
import com.stanzaliving.user.service.OtpService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/internal/otp")
public class InternalOtpController {

	@Autowired
	private OtpService otpService;

	@PostMapping("mobile/request")
	public ResponseDto<Void> sendMobileOtp(@RequestBody MobileOtpRequestDto mobileOtpRequestDto) {

		log.info("Received request to send OTP: {}", mobileOtpRequestDto);

		otpService.sendMobileOtp(mobileOtpRequestDto);

		return ResponseDto.success("OTP sent to mobile");
	}

	@PostMapping("moible/validate")
	public ResponseDto<Void> validateMobileOtp(@RequestBody MobileOtpValidateRequestDto mobileOtpValidateRequestDto) {

		log.info("Received request to validate OTP: {}", mobileOtpValidateRequestDto);

		try {

			otpService.validateMobileOtp(
					mobileOtpValidateRequestDto.getMobile(),
					mobileOtpValidateRequestDto.getIsoCode(),
					mobileOtpValidateRequestDto.getOtp(),
					mobileOtpValidateRequestDto.getOtpType());

			return ResponseDto.success("OTP Succefully Validated");

		} catch (AuthException e) {
			log.error(e.getMessage());
			return ResponseDto.failure(e.getMessage());
		}
	}

	@PostMapping("mobile/resend")
	public ResponseDto<Void> resendMobileOtp(@RequestBody MobileOtpRequestDto mobileOtpRequestDto) {

		log.info("Received request to resend OTP: {}", mobileOtpRequestDto);

		try {

			otpService.resendMobileOtp(mobileOtpRequestDto.getMobile(), mobileOtpRequestDto.getIsoCode(), mobileOtpRequestDto.getOtpType());

			return ResponseDto.success("OTP resent to mobile");

		} catch (AuthException e) {
			log.error(e.getMessage());
			return ResponseDto.failure(e.getMessage());
		}
	}
}