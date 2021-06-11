package com.stanzaliving.user.controller.internal;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.user.service.UserService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/internal/details")
public class InternalUserDetailsController {

	@Autowired
	private UserService userService;

	@GetMapping("{userUuid}")
	public ResponseDto<UserProfileDto> getUser(@PathVariable String userUuid) {

		log.info("Fetching User with userUuid: {}", userUuid);

		return ResponseDto.success("Found User for userUuid", userService.getActiveUserByUserId(userUuid));
	}

	@GetMapping("all")
	public ResponseDto<List<UserProfileDto>> getAllUsers() {

		log.info("Fetching All User");

		return ResponseDto.success("Found Users", userService.getAllUsers());
		
	}
	
	@PostMapping("user/all")
	public ResponseDto<List<UserProfileDto>> getAllUsersByUuidInAndStatus(@RequestBody Set<String> uuids) {

		log.info("Fetching All User");

		return ResponseDto.success("Found Users", userService.getAllUsersByUuidInAndStatus(uuids));
		

	}
	
	@GetMapping("/mobile")
	public ResponseDto<UserProfileDto> getUserDetails(@RequestParam(name = "mobileNo", required = true) String mobileNo) {

		log.info("Fetching User with mobileNo: {}", mobileNo);

		return ResponseDto.success("Found User for userUuid", userService.getUserDetails(mobileNo));
	}

}