package com.stanzaliving.user.acl.adapters;

import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.user.entity.SignupEntity;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SignUpAdapter {

	public static SignupEntity getSignupEntity(AddUserRequestDto addUserRequestDto, int otp) {

		SignupEntity signup = SignupEntity.builder().otp(otp).mobile(addUserRequestDto.getMobile())
				.validated(Boolean.FALSE).signupObject(addUserRequestDto).build();

		return signup;
	}

	public static UserDto getUserDto(AddUserRequestDto addUserRequestDto, SignupEntity signUp) {

		return UserDto.builder().uuid(signUp.getUuid()).createdAt(signUp.getCreatedAt())
				.updatedAt(signUp.getUpdatedAt()).createdBy(signUp.getCreatedBy()).updatedBy(signUp.getUpdatedBy())
				.status(signUp.isStatus()).userType(addUserRequestDto.getUserType())
				.isoCode(addUserRequestDto.getIsoCode()).mobile(addUserRequestDto.getMobile())
				.email(addUserRequestDto.getEmail()).department(addUserRequestDto.getDepartment())
				.firstName(addUserRequestDto.getFirstName())
				.lastName(addUserRequestDto.getLastName())
				.otp(signUp.getOtp())
				.departmentName(addUserRequestDto.getDepartment().getDepartmentName()).build();
	}

}
