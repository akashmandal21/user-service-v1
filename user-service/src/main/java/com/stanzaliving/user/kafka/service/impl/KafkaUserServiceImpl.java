/**
 * 
 */
package com.stanzaliving.user.kafka.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.enums.SmsType;
import com.stanzaliving.core.kafka.producer.NotificationProducer;
import com.stanzaliving.core.pojo.EmailDto;
import com.stanzaliving.core.pojo.SmsDto;
import com.stanzaliving.core.property.manager.PropertyManager;
import com.stanzaliving.core.user.constants.UserErrorCodes;
import com.stanzaliving.core.user.constants.UserErrorCodes.Otp;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.user.constants.UserConstants;
import com.stanzaliving.user.entity.OtpEntity;
import com.stanzaliving.user.kafka.service.KafkaUserService;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen
 *
 * @date 12-Oct-2019
 */
@Log4j2
@Service
public class KafkaUserServiceImpl implements KafkaUserService {

	@Autowired
	private PropertyManager propertyManager;

	@Autowired
	private ThreadPoolTaskExecutor userExecutor;

	@Autowired
	private NotificationProducer notificationProducer;

	@Override
	public void sendOtpToKafka(OtpEntity otpEntity) {
		try {
			userExecutor.execute(() -> {
				sendOtpOnMobile(otpEntity);
				sendOtpOnMail(otpEntity);
			});

		} catch (Exception e) {
			log.error("OTP Queue Overflow : ", e);
			throw new StanzaException(Otp.ERROR_SENDING_OTP, e);
		}
	}

	private void sendOtpOnMobile(OtpEntity otpEntity) {
		SmsDto otpDto = getSms(otpEntity);

		sendMessage(otpDto);
	}

	private SmsDto getSms(OtpEntity otpEntity) {
		return SmsDto.builder()
				.smsType(SmsType.OTP)
				.isoCode(otpEntity.getIsoCode())
				.mobile(otpEntity.getMobile())
				.text(getOtpMessageForUserType(otpEntity))
				.build();
	}

	private void sendMessage(SmsDto smsDto) {

		String smsTopic = propertyManager.getProperty("kafka.topic.sms", "sms");

		if (SmsType.OTP == smsDto.getSmsType()) {
			smsTopic = propertyManager.getProperty("kafka.topic.sms.otp", "sms_otp");
		}

		notificationProducer.publish(smsTopic, SmsDto.class.getName(), smsDto);
	}

	@Override
	public void sendSmsToKafka(SmsDto smsDto) {
		try {
			userExecutor.execute(() -> sendMessage(smsDto));
		} catch (Exception e) {
			log.error("SMS Queue Overflow : ", e);
			throw new StanzaException(UserErrorCodes.ERROR_SENDING_SMS, e);
		}
	}

	private void sendOtpOnMail(OtpEntity otpEntity) {
		if (propertyManager.getPropertyAsBoolean("otp.email.enabled")
				&& StringUtils.isNotBlank(otpEntity.getEmail())
				&& UserType.STUDENT == otpEntity.getUserType()) {

			try {
				EmailDto emailDto = getEmail(otpEntity);
				log.debug("Sending OTP on Email for user: " + otpEntity.getUserId());
				notificationProducer.publish(propertyManager.getProperty("kafka.topic.email.otp", "email_otp"), EmailDto.class.getName(), emailDto);
			} catch (Exception e) {
				log.error("Error sending OTP on Email for user: " + otpEntity.getUserId(), e);
			}
		}
	}

	private EmailDto getEmail(OtpEntity otpEntity) {
		EmailDto emailDto = new EmailDto();

		emailDto.setTo(new String[] { otpEntity.getEmail() });

		emailDto.setSubject("OTP to access Stanza Living");

		emailDto.setContent(getOtpMessageForUserType(otpEntity));

		return emailDto;
	}

	private String getOtpMessageForUserType(OtpEntity otpEntity) {
		String message;

		switch (otpEntity.getUserType()) {
			case STUDENT:
				message = propertyManager.getProperty("student.otp.msg", UserConstants.DEFAULT_OTP_TEXT);
				break;
			case PARENT:
				message = propertyManager.getProperty("parent.otp.msg", UserConstants.DEFAULT_OTP_TEXT);
				break;
			case LEGAL:
				message = propertyManager.getProperty("legal.otp.msg", UserConstants.DEFAULT_OTP_TEXT);
				break;
			case HR:
				message = propertyManager.getProperty("hr.otp.msg", UserConstants.DEFAULT_OTP_TEXT);
				break;
			case TECH:
				message = propertyManager.getProperty("tech.otp.msg", UserConstants.DEFAULT_OTP_TEXT);
				break;
			case FINANCE:
				message = propertyManager.getProperty("finance.otp.msg", UserConstants.DEFAULT_OTP_TEXT);
				break;
			case PROCUREMENT:
				message = propertyManager.getProperty("procurement.otp.msg", UserConstants.DEFAULT_OTP_TEXT);
				break;
			default:
				message = propertyManager.getProperty("default.otp.msg", UserConstants.DEFAULT_OTP_TEXT);
		}

		message = message.replaceAll("<otp>", String.valueOf(otpEntity.getOtp()));

		return message;
	}
}