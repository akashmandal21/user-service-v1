/**
 * 
 */
package com.stanzaliving.user.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.StanzaConstants;
import com.stanzaliving.core.base.utils.DateUtil;
import com.stanzaliving.core.base.utils.PhoneNumberUtils;
import com.stanzaliving.core.base.utils.StanzaUtils;
import com.stanzaliving.core.user.constants.UserErrorCodes.Otp;
import com.stanzaliving.core.user.enums.OtpType;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.core.user.request.dto.LoginRequestDto;
import com.stanzaliving.core.user.request.dto.OtpValidateRequestDto;
import com.stanzaliving.user.db.service.OtpDbService;
import com.stanzaliving.user.entity.OtpEntity;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.exception.AuthException;
import com.stanzaliving.user.kafka.service.KafkaUserService;
import com.stanzaliving.user.service.OtpService;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Log4j2
@Service
public class OtpServiceImpl implements OtpService {

	@Value("${environment.type}")
	private String environment;

	@Value("${test.mobile}")
	private String testMobile;

	@Value("${otp.length:4}")
	private int otpLength;

	@Value("${otp.expiry.minutes:5}")
	private int otpExpiryMinutes;

	@Value("${otp.max.resend.count:5}")
	private int otpMaxResendCount;

	@Value("${otp.resend.enable.seconds:30}")
	private int otpResendEnableSeconds;

	@Autowired
	private OtpDbService otpDbService;

	@Autowired
	private KafkaUserService kafkaUserService;

	@Override
	public void sendLoginOtp(UserEntity userEntity) {

		OtpEntity currentOtp = otpDbService.getUserOtpByUserId(userEntity.getUuid(), OtpType.LOGIN);
		OtpEntity userOtp;

		if (currentOtp == null
				|| !(userEntity.getMobile().equals(currentOtp.getMobile()) && userEntity.getIsoCode().equals(currentOtp.getIsoCode()))) {
			userOtp = createOtpForUser(userEntity, OtpType.LOGIN);
		} else {
			currentOtp.setResendCount(0);
			userOtp = updateUserOtp(currentOtp);
		}

		log.info("Sending OTP: " + userOtp.getOtp() + " for User: " + userOtp.getUserId() + " for login");
		kafkaUserService.sendOtpToKafka(userOtp);
	}

	private OtpEntity createOtpForUser(UserEntity user, OtpType otpType) {
		OtpEntity userOtp = new OtpEntity();

		userOtp.setUserId(user.getUuid());
		userOtp.setUserType(user.getUserType());

		userOtp.setMobile(PhoneNumberUtils.normalizeNumber(user.getMobile()));
		userOtp.setIsoCode(user.getIsoCode());

		userOtp.setEmail(user.getEmail());

		userOtp.setOtp(generateOtp(userOtp));
		userOtp.setOtpType(otpType);

		userOtp.setResendCount(0);
		userOtp = otpDbService.saveAndFlush(userOtp);

		return userOtp;
	}

	/**
	 * Method to generate fix OTP for dev & test environments and configured
	 * mobile numbers and dynamic OTP for rest if the cases
	 * 
	 * @return OTP to send
	 */
	private Integer generateOtp(OtpEntity userOtp) {
		return isTestEnvironment() || isTestMobile(userOtp) ? StanzaUtils.generateDefaultOtpOfLength(otpLength) : StanzaUtils.generateOTPOfLength(otpLength);
	}

	private boolean isTestEnvironment() {
		return StringUtils.isNotBlank(environment)
				&& ("dev".equalsIgnoreCase(environment)
						|| "staging".equalsIgnoreCase(environment)
						|| "test".equalsIgnoreCase(environment));
	}

	private boolean isTestMobile(OtpEntity userOtp) {

		if (StringUtils.isNotBlank(testMobile)
				&& StringUtils.isNotBlank(userOtp.getMobile())
				&& StringUtils.isNotBlank(userOtp.getIsoCode())) {

			String[] mobileNos = testMobile.split(",");

			Set<String> mobiles = Arrays.stream(mobileNos).collect(Collectors.toSet());

			return mobiles.contains(userOtp.getMobile());
		}

		return false;
	}

	private OtpEntity updateUserOtp(OtpEntity userOtp) {
		userOtp.setOtp(generateOtp(userOtp));
		userOtp.setStatus(true);

		log.debug("Updating OTP for User: " + userOtp.getUserId());
		userOtp = otpDbService.updateAndFlush(userOtp);

		return userOtp;
	}

	@Override
	public void validateLoginOtp(OtpValidateRequestDto otpValidateRequestDto) {
		validateMobileOtp(otpValidateRequestDto, OtpType.LOGIN);
	}

	@Override
	public void validateMobileOtp(OtpValidateRequestDto otpValidateRequestDto, OtpType otpType) {
		OtpEntity userOtp =
				getLastActiveOtp(otpValidateRequestDto.getMobile(), otpValidateRequestDto.getIsoCode(),otpValidateRequestDto.getUserType(), otpType);

		compareOTP(otpValidateRequestDto.getOtp(), userOtp);

		log.debug("OTP validated for mobile: " + userOtp.getMobile());

		expireOtp(userOtp);
	}

	private OtpEntity getLastActiveOtp(String mobile, String isoCode,UserType userType, OtpType otpType) {

		return otpDbService.getActiveOtpForMobile(
				PhoneNumberUtils.normalizeNumber(mobile), otpType,userType, isoCode);

	}

	private void compareOTP(String otp, OtpEntity userOtp) {
		Date otpTime = new Date(
				LocalDateTime.now(StanzaConstants.IST_TIMEZONEID).atZone(StanzaConstants.IST_TIMEZONEID).toInstant().getEpochSecond() - (otpExpiryMinutes * 60000));

		if (userOtp == null
				|| userOtp.getOtp() == null
				|| !userOtp.isStatus()
				|| userOtp.getUpdatedAt() == null
				|| userOtp.getUpdatedAt().before(otpTime)
				|| !userOtp.getOtp().toString().equals(otp)) {

			throw new AuthException("Invalid OTP For User", Otp.INVALID_OTP);
		}
	}

	private void expireOtp(OtpEntity otp) {
		otp.setStatus(false);

		log.debug("Marking OTP Expired for User: " + otp.getUserId());

		otpDbService.updateAndFlush(otp);
	}

	@Override
	public void resendLoginOtp(LoginRequestDto loginRequestDto) {
		resendMobileOtp(loginRequestDto, OtpType.LOGIN);
	}

	@Override
	public void resendMobileOtp(LoginRequestDto loginRequestDto, OtpType otpType) {
		OtpEntity userOtp = getLastActiveOtp(loginRequestDto.getMobile(), loginRequestDto.getIsoCode(),loginRequestDto.getUserType(), otpType);

		if (userOtp == null) {

			throw new AuthException(
					"No OTP found for Mobile: " + loginRequestDto.getMobile() + ", ISOCode: " + loginRequestDto.getIsoCode() + ", OtpType: " + otpType,
					Otp.OTP_NOT_FOUND);

		} else {

			if (userOtp.getResendCount() >= otpMaxResendCount) {
				throw new AuthException("Resend OTP can be used maximum " + otpMaxResendCount + " times.", Otp.OTP_RESEND_LIMIT_EXHAUSTED);
			}
			LocalDateTime currentTime = LocalDateTime.now(StanzaConstants.IST_TIMEZONEID);
			LocalDateTime otpResendEnableTime = DateUtil.convertToLocalDateTime(userOtp.getUpdatedAt()).plusSeconds(otpResendEnableSeconds);

			if (currentTime.isBefore(otpResendEnableTime)) {

				long secondsRemaining = ChronoUnit.SECONDS.between(currentTime, otpResendEnableTime);

				throw new AuthException(
						"OTP Can be Resent Only After " + secondsRemaining + " Seconds", Otp.OTP_RESEND_NOT_PERMITTED);
			}

		}

		userOtp.setResendCount(userOtp.getResendCount() + 1);
		log.debug("Updating OTP for User: " + userOtp.getUserId());
		userOtp = otpDbService.updateAndFlush(userOtp);

		log.info("Re-Sending OTP: " + userOtp.getOtp() + " for User: " + userOtp.getUserId() + " of Type " + otpType);
		kafkaUserService.sendOtpToKafka(userOtp);
	}

}