package com.stanzaliving.user.service.impl;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.exception.AuthException;
import com.stanzaliving.core.base.utils.StanzaUtils;
import com.stanzaliving.core.user.constants.UserErrorCodes.Otp;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.user.acl.adapters.SignUpAdapter;
import com.stanzaliving.user.adapters.UserAdapter;
import com.stanzaliving.user.db.service.SignUpDbService;
import com.stanzaliving.user.db.service.UserDbService;
import com.stanzaliving.user.entity.OtpEntity;
import com.stanzaliving.user.entity.SignupEntity;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.kafka.service.KafkaUserService;
import com.stanzaliving.user.service.SignUpService;
import com.stanzaliving.user.service.UserService;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class SignUpServiceImpl implements SignUpService {

	@Value("${otp.length:4}")
	private int otpLength;

	@Value("${kafka.resident.detail.topic}")
	private String kafkaResidentDetailTopic;

	@Autowired
	private KafkaUserService kafkaUserService;

	@Autowired
	private SignUpDbService signUpDbService;
	@Autowired
	private UserService userService;

	@Autowired
	private UserDbService userDbService;

	@Override
	public String signUpUser(AddUserRequestDto addUserRequestDto) {

		int otp = StanzaUtils.generateOTPOfLength(otpLength);

		SignupEntity signUp = SignUpAdapter.getSignupEntity(addUserRequestDto, otp);

		signUp = signUpDbService.save(signUp);

		sendOtp(addUserRequestDto, signUp);

		return signUp.getUuid();
	}

	@Override
	public UserProfileDto validateSignUpOtp(String uuid, String otp) {

		SignupEntity signup = signUpDbService.findByUuidAndStatus(uuid, Boolean.TRUE);

		compareOTP(otp, signup);
		UserDto userDto = userService.addUser(signup.getSignupObject());
		if (Objects.isNull(userDto))
			throw new AuthException("user Not created.");

		UserEntity userEntity = userDbService.getUserForMobile(signup.getMobile(), "IN");

		return UserAdapter.getUserProfileDto(userEntity);
	}

	private boolean compareOTP(String otp, SignupEntity signup) {

		if (signup == null)
			throw new AuthException("No OTP exists for mobile", Otp.OTP_NOT_FOUND);

		if (!signup.isValidated() && signup.getOtp().toString().equals(otp)) {
			signup.setValidated(true);
			signUpDbService.save(signup);
		} else {
			throw new AuthException("Invalid OTP For User With Mobile " + signup.getMobile(), Otp.INVALID_OTP);
		}

		return Boolean.TRUE;
	}

	private void sendOtp(AddUserRequestDto addUserRequestDto, SignupEntity signUpOtp) {

		OtpEntity userOtp = OtpEntity.builder().otp(signUpOtp.getOtp()).mobile(signUpOtp.getMobile())
				.userType(addUserRequestDto.getUserType()).status(Boolean.TRUE).build();

		log.info("Sending Oto for Signup, {}", userOtp);

		kafkaUserService.sendOtpToKafka(userOtp);

	}

}
