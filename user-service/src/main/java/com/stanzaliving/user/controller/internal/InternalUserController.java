/**
 * 
 */
package com.stanzaliving.user.controller.internal;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.core.user.request.dto.UpdateUserRequestDto;
import com.stanzaliving.user.service.UserService;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Log4j2
@RestController
@RequestMapping("internal")
public class InternalUserController {

	@Autowired
	private UserService userService;

	@PostMapping("update")
	public ResponseDto<UserDto> updateUser(@RequestBody @Valid UpdateUserRequestDto updateUserRequestDto) {

		UserDto userDto = userService.updateUser(updateUserRequestDto);

		log.info("Update user with id: " + userDto.getUuid());

		return ResponseDto.success("User Updated", userDto);
	}

	@PostMapping("update/mobile")
	public ResponseDto<UserDto> updateUserMobile(@RequestBody @Valid UpdateUserRequestDto updateUserRequestDto) {

		UserDto userDto = userService.updateUserMobile(updateUserRequestDto);

		log.info("Update user with id: " + userDto.getUuid());

		return ResponseDto.success("User Updated", userDto);
	}

	@PostMapping("add")
	public ResponseDto<UserDto> addUser(@RequestBody @Valid AddUserRequestDto addUserRequestDto) {

		UserDto userDto = userService.addUser(addUserRequestDto);

		log.info("Added new user with id: " + userDto.getUuid());

		return ResponseDto.success("New User Created", userDto);
	}

	@PostMapping("deactivate/{userId}")
	public ResponseDto<Boolean> addUser(@PathVariable String userId) {

		boolean status = userService.updateUserStatus(userId, false);

		log.info("Deactivated user with id: " + userId);

		return (status) ? ResponseDto.success(status) : ResponseDto.failure("Unable to deactivate user");
	}

	@PutMapping("update/status/{mobileNo}/{userType}")
	public ResponseDto<Boolean> updateUserStatus(@PathVariable("mobileNo") String mobileNo,
			@PathVariable("userType") String userType) {

		boolean status = userService.updateUserStatus(mobileNo, userType);

		log.info("Status Update true for Mobile No {} ", mobileNo);

		return (status) ? ResponseDto.success("Status Update Successfully", status)
				: ResponseDto.failure("Unable to Update user Status");
	}

	@PutMapping("update/userType/{mobileNo}/{userType}")
	public ResponseDto<UserDto> updateUserType(@PathVariable("mobileNo") String mobileNo,
			@PathVariable("userType") String userType) {

		UserDto userDto = userService.updateUserType(mobileNo, userType);

		log.info("Update userType: " + userDto.getUuid());

		return ResponseDto.success("User Updated", userDto);
	}

}