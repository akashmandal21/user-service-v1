/**
 * 
 */
package com.stanzaliving.user.acl.controller;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.acl.request.dto.UserAccessDto;
import com.stanzaliving.user.acl.service.impl.AclServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @author naveen.kumar
 *
 * @date 23-Oct-2019
 *
 **/
@Log4j2
@RestController
@RequestMapping("acl")
public class AclController {

	@Autowired
	private AclServiceImpl aclService;

	@PostMapping("check")
	public ResponseDto<Boolean> isAccessible(@RequestBody @Valid UserAccessDto userAccessDto) {

		log.info("Checking User: " + userAccessDto.getUserId() + " access for url: " + userAccessDto.getUrl());

		boolean accessible = aclService.isAccesible(userAccessDto.getUserId(), userAccessDto.getUrl());

		log.info("URL: " + userAccessDto.getUrl() + " accessible by user: " + userAccessDto.getUserId() + " Status: " + accessible);

		return ResponseDto.success("URL Access Status: " + accessible, accessible);
	}

	@GetMapping("getDepartmentRoleMap")
	public ResponseDto<Map<Department, List<String>>> getDepartmentRoleMap(@RequestParam String userUuid) {
		log.info("Received request to get DepartmentRoleMap for user " + userUuid);
		return aclService.getDepartmentRoleMap(userUuid);
	}

}