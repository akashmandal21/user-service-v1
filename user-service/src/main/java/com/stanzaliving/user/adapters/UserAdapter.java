/**
 * 
 */
package com.stanzaliving.user.adapters;

import com.stanzaliving.core.sqljpa.adapter.AddressAdapter;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.entity.UserProfileEntity;

import lombok.experimental.UtilityClass;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@UtilityClass
public class UserAdapter {

	public static UserProfileDto getUserDto(UserEntity userEntity) {

		return UserProfileDto.builder()
				.uuid(userEntity.getUuid())
				.createdAt(userEntity.getCreatedAt())
				.updatedAt(userEntity.getUpdatedAt())
				.createdBy(userEntity.getCreatedBy())
				.updatedBy(userEntity.getUpdatedBy())
				.status(userEntity.isStatus())
				.userType(userEntity.getUserType())
				.isoCode(userEntity.getIsoCode())
				.mobile(userEntity.getMobile())
				.mobileVerified(userEntity.isMobileVerified())
				.email(userEntity.getEmail())
				.emailVerified(userEntity.isEmailVerified())
				.firstName(userEntity.getUserProfile().getFirstName())
				.lastName(userEntity.getUserProfile().getLastName())
				.build();
	}

	public static UserProfileEntity getUserProfileEntity(AddUserRequestDto addUserRequestDto) {

		return UserProfileEntity.builder()
				.firstName(addUserRequestDto.getFirstName())
				.middleName(addUserRequestDto.getMiddleName())
				.lastName(addUserRequestDto.getLastName())
				.secondaryEmail(addUserRequestDto.getSecondaryEmail())
				.secondaryEmailVerified(false)
				.secondaryIsoCode(addUserRequestDto.getSecondaryIsoCode())
				.secondaryMobile(addUserRequestDto.getSecondaryMobile())
				.secondaryMobileVerified(false)
				.gender(addUserRequestDto.getGender())
				.profilePicture(addUserRequestDto.getProfilePicture())
				.birthday(addUserRequestDto.getBirthday())
				.maritalStatus(addUserRequestDto.getMaritalStatus())
				.anniversaryDate(addUserRequestDto.getAnniversaryDate())
				.address(AddressAdapter.getAddressEntity(addUserRequestDto.getAddress()))
				.build();
	}

	public static UserProfileDto getUserProfileDto(UserEntity userEntity) {

		UserProfileEntity profileEntity = userEntity.getUserProfile();

		return UserProfileDto.builder()
				.uuid(userEntity.getUuid())
				.createdAt(userEntity.getCreatedAt())
				.updatedAt(userEntity.getUpdatedAt())
				.createdBy(userEntity.getCreatedBy())
				.updatedBy(userEntity.getUpdatedBy())
				.status(userEntity.isStatus())
				.userType(userEntity.getUserType())
				.isoCode(userEntity.getIsoCode())
				.mobile(userEntity.getMobile())
				.mobileVerified(userEntity.isMobileVerified())
				.email(userEntity.getEmail())
				.emailVerified(userEntity.isEmailVerified())
				.firstName(profileEntity.getFirstName())
				.middleName(profileEntity.getMiddleName())
				.lastName(profileEntity.getLastName())
				.secondaryEmail(profileEntity.getSecondaryEmail())
				.secondaryEmailVerified(profileEntity.isSecondaryEmailVerified())
				.secondaryIsoCode(profileEntity.getSecondaryIsoCode())
				.secondaryMobile(profileEntity.getSecondaryMobile())
				.secondaryMobileVerified(profileEntity.isSecondaryMobileVerified())
				.gender(profileEntity.getGender())
				.profilePicture(profileEntity.getProfilePicture())
				.birthday(profileEntity.getBirthday())
				.maritalStatus(profileEntity.getMaritalStatus())
				.anniversaryDate(profileEntity.getAnniversaryDate())
				.address(AddressAdapter.getAddressDto(profileEntity.getAddress()))
				.build();
	}
}