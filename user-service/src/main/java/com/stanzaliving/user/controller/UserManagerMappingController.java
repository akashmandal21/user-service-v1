/**
 * 
 */
package com.stanzaliving.user.controller;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.user.request.dto.UserManagerMappingRequestDto;
import com.stanzaliving.user.service.UserManagerMappingService;

import lombok.extern.log4j.Log4j2;

/**
 * @author raj.kumar
 *
 */
@Log4j2
@RestController
@RequestMapping("usermanagermapping")
public class UserManagerMappingController {

	@Autowired
	private UserManagerMappingService userManagerMappingService;
	
	@PostMapping("create")
	public ResponseDto<Void> createMapping(@RequestBody @Valid UserManagerMappingRequestDto userManagerMappingRequestDto) {
		log.info(" Create userId and managerId mapping for : " + userManagerMappingRequestDto.getUserId() + " " + userManagerMappingRequestDto.getManagerId());

		userManagerMappingService.createUserManagerMapping(userManagerMappingRequestDto);		
		return ResponseDto.success("Mapping Created Successfully ");
	}
	
	@GetMapping("{managerId}")
	public ResponseDto<List<String>> getUserIdsByManagerId(@PathVariable(name = "managerId") @NotBlank(message = "Manager Id is Mandatory") String managerId) {
		log.info(" Get mapped userIds by managerId " + managerId);

		List<String> userIds = userManagerMappingService.getUserIdsMappedWithManagerId(managerId);		
		return ResponseDto.success("Mapping Created Successfully", userIds);
	}

	@GetMapping("/managername/{userId}")
	public ResponseDto<String> getManagerNameByUserID(@PathVariable(name = "userId") @NotBlank(message = "User Id is Mandatory") String userId) {
		log.info(" Get manager name by " + userId);

		String managerName = userManagerMappingService.findManagerNameForUser(userId);		
		return ResponseDto.success("Manager Name Found!", managerName);
	}

}
