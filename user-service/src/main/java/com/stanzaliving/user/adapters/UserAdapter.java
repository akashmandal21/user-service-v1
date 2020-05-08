/**
 * 
 */
package com.stanzaliving.user.adapters;

import com.stanzaliving.core.sqljpa.adapter.AddressAdapter;
import com.stanzaliving.core.user.acl.dto.AclUserDto;
import com.stanzaliving.core.user.acl.dto.AclUserProfileDTO;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleNameUrlExpandedDto;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.dto.response.UserContactDetailsResponseDto;
import com.stanzaliving.core.user.enums.EnumListing;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.entity.UserProfileEntity;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@UtilityClass
public class UserAdapter {

	public static UserDto getUserDto(UserEntity userEntity) {

		return UserDto.builder()
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
				.department(userEntity.getDepartment())
				.departmentName(userEntity.getDepartment().departmentName)
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
		if(Objects.isNull(profileEntity)){
			return null;
		}

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
				.department(userEntity.getDepartment())
				.departmentName(userEntity.getDepartment().getDepartmentName())
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

	public static AclUserDto getAclUserDto(UserProfileDto userDto, List<UserDeptLevelRoleNameUrlExpandedDto> acl) {
		
		return AclUserDto.builder()
				.uuid(userDto.getUuid())
				.createdAt(userDto.getCreatedAt())
				.updatedAt(userDto.getUpdatedAt())
				.createdBy(userDto.getCreatedBy())
				.updatedBy(userDto.getUpdatedBy())
				.status(userDto.getStatus())
				.userType(userDto.getUserType())
				.isoCode(userDto.getIsoCode())
				.mobile(userDto.getMobile())
				.mobileVerified(userDto.getMobileVerified())
				.email(userDto.getEmail())
				.emailVerified(userDto.getEmailVerified())
				.department(userDto.getDepartment())
				.firstName(userDto.getFirstName())
				.lastName(userDto.getLastName())
				.acl(acl)
				.build();
	}

	public static AclUserProfileDTO getAclUserProfileDTO(UserProfileDto userProfileDto, List<UserDeptLevelRoleNameUrlExpandedDto> acl) {

		return AclUserProfileDTO.builder()
				.uuid(userProfileDto.getUuid())
				.createdAt(userProfileDto.getCreatedAt())
				.updatedAt(userProfileDto.getUpdatedAt())
				.createdBy(userProfileDto.getCreatedBy())
				.updatedBy(userProfileDto.getUpdatedBy())
				.status(userProfileDto.getStatus())
				.userType(userProfileDto.getUserType())
				.isoCode(userProfileDto.getIsoCode())
				.mobile(userProfileDto.getMobile())
				.mobileVerified(userProfileDto.getMobileVerified())
				.email(userProfileDto.getEmail())
				.emailVerified(userProfileDto.getEmailVerified())
				.department(userProfileDto.getDepartment())
				.firstName(userProfileDto.getFirstName())
				.middleName(userProfileDto.getMiddleName())
				.lastName(userProfileDto.getLastName())
				.secondaryEmail(userProfileDto.getSecondaryEmail())
				.secondaryEmailVerified(userProfileDto.isSecondaryEmailVerified())
				.secondaryIsoCode(userProfileDto.getSecondaryIsoCode())
				.secondaryMobile(userProfileDto.getSecondaryMobile())
				.secondaryMobileVerified(userProfileDto.isSecondaryMobileVerified())
				.gender(userProfileDto.getGender())
				.profilePicture(userProfileDto.getProfilePicture())
				.birthday(userProfileDto.getBirthday())
				.maritalStatus(userProfileDto.getMaritalStatus())
				.anniversaryDate(userProfileDto.getAnniversaryDate())
				.address(userProfileDto.getAddress())
				.acl(acl)
				.build();

	}



	public List<EnumListing> getUserTypeEnumAsListing() {
		List<EnumListing> data = new ArrayList<>();
		for (UserType type: UserType.values()) {
			data.add(
					EnumListing.builder()
							.key(type.name())
							.value(type.typeName)
							.build()
			);
		}
		return data;
	}

	public UserContactDetailsResponseDto convertToContactResponseDto(UserEntity userEntity) {
		UserProfileEntity userProfile = userEntity.getUserProfile();
		String name = null;
		if (Objects.nonNull(userProfile)) {
			name = userProfile.getFirstName().concat(userProfile.getMiddleName()).concat(userProfile.getLastName());
		}

		return UserContactDetailsResponseDto.builder()
				.userId(userEntity.getUuid())
				.email(userEntity.getEmail())
				.mobile(userEntity.getMobile())
				.name(name)
				.build();
	}


}