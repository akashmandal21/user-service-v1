/**
 * 
 */
package com.stanzaliving.user.acl.controller;

import java.util.List;

import com.stanzaliving.core.user.dto.response.UserContactDetailsResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.user.acl.service.AclUserService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/internal/acl/")
public class AclInternalController {

	@Autowired
	private AclUserService aclUserService;
	
	@GetMapping("/useridByRoleName/{department}/{roleName}/{accessLevelId}")
	public ResponseDto<List<String>> getUserIds(@PathVariable Department department,@PathVariable String roleName,@PathVariable String accessLevelId ) {

		log.info("Fetching user by {},{},{}", department,roleName,accessLevelId);

		return ResponseDto.success("Found User", aclUserService.getUsersForRoles(department, roleName, accessLevelId));
	}

	@GetMapping("/usercontactdetails/{department}/{roleName}/{accessLevelId}")
	public ResponseDto<List<UserContactDetailsResponseDto>> getUserContactDetails(
			@PathVariable Department department,
			@PathVariable String roleName,
			@PathVariable String accessLevelId
	) {
		log.info("Received user contact details request by department {}, roleName {} and accessLevelId {}", department, roleName, accessLevelId);
		return ResponseDto.success("Found contact details of users", aclUserService.getUserContactDetails(department, roleName, accessLevelId));
	}

}