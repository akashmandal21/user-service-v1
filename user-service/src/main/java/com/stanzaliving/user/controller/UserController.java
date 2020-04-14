/**
 * 
 */
package com.stanzaliving.user.controller;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.constants.SecurityConstants;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.base.utils.CSVConverter;
import com.stanzaliving.core.user.acl.dto.AclUserProfileDTO;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.user.acl.service.AclService;
import com.stanzaliving.user.adapters.UserAdapter;
import com.stanzaliving.user.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Log4j2
@RestController
@RequestMapping("")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	AclService aclService;

	@GetMapping("pingMe")
	public ResponseDto<String> pingMe() {
		return ResponseDto.success("I am working");
	}

	@GetMapping("details")
	public ResponseDto<UserProfileDto> getUser(
			@RequestAttribute(name = SecurityConstants.USER_ID) @NotBlank(message = "User Id is mandatory to get user") String userId) {

		log.info("Fetching User with UserId: " + userId);

		return ResponseDto.success("Found User for User Id", userService.getActiveUserByUserId(userId));
	}

	@GetMapping("profile")
	public ResponseDto<AclUserProfileDTO> getUserProfile(
			@RequestAttribute(name = SecurityConstants.USER_ID) @NotBlank(message = "User Id is mandatory to get user profile") String userId) {

		log.info("Fetching User Profile with UserId: " + userId);

		return ResponseDto.success("Found User Profile for User Id", UserAdapter.getAclUserProfileDTO(userService.getUserProfile(userId), aclService.getUserDeptLevelRoleNameUrlExpandedDtoFe(userId)));
	}

	@PostMapping("add")
	public ResponseDto<UserDto> addUser(@RequestBody @Valid AddUserRequestDto addUserRequestDto) {

		UserDto userDto = userService.addUser(addUserRequestDto);

		log.info("Added new user with id: " + userDto.getUuid());

		return ResponseDto.success("New User Created", userDto);
	}

	@GetMapping("search/{pageNo}/{limit}")
	public ResponseDto<PageResponse<UserProfileDto>> searchUsers(
			@PathVariable(name = "pageNo") @Min(value = 1, message = "Page No must be greater than 0") int pageNo,
			@PathVariable(name = "limit") @Min(value = 1, message = "Limit must be greater than 0") int limit,
			@RequestParam(name = "userIds", required = false) List<String> userIds,
			@RequestParam(name = "mobile", required = false) String mobile,
			@RequestParam(name = "isoCode", required = false) String isoCode,
			@RequestParam(name = "email", required = false) String email,
			@RequestParam(name = "userType", required = false) UserType userType,
			@RequestParam(name = "status", required = false) Boolean status) {

		log.info("Received User Search Request With Parameters [Page: " + pageNo + ", Limit: " + limit + ", Mobile: " + mobile + ", ISO: " + isoCode + ", Email: " + email + ", UserType: " + userType
				+ ", Status: " + status + ", UserIds: {" + CSVConverter.getCSVString(userIds) + "} ]");

		PageResponse<UserProfileDto> userDtos = userService.searchUser(userIds, mobile, isoCode, email, userType, status, pageNo, limit);

		return ResponseDto.success("Found " + userDtos.getRecords() + " Users for Search Criteria", userDtos);
	}


	@GetMapping("type/list")
	public ResponseDto<UserType[]> getUserType() {

		log.info("Received UserType listing request.");

		return ResponseDto.success("Found UserType", UserType.values());
	}


	@GetMapping("department/list")
	public ResponseDto<Department[]> getUserDepartment() {

		log.info("Received Department listing request.");

		return ResponseDto.success("Found Department", Department.values());
	}


	@PostMapping("update/userStatus")
	public ResponseDto<Boolean> updateUserStatus(
			@RequestParam("userid") String userUuid,
			@RequestParam("status") Boolean status
	) {
		log.info("Received request to deactivate user");
		return ResponseDto.success("Successfully deactived user.", userService.updateUserStatus(userUuid));
	}


}