/**
 * 
 */
package com.stanzaliving.user.acl.controller;

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
import com.stanzaliving.core.user.acl.dto.RoleMetadataDto;
import com.stanzaliving.core.user.acl.request.dto.AssignUserRoleDto;
import com.stanzaliving.user.acl.service.UserRoleService;

import lombok.extern.log4j.Log4j;

/**
 * @author naveen.kumar
 *
 * @date 23-Oct-2019
 *
 **/
@Log4j
@RestController
@RequestMapping("acl/userrole")
public class UserRoleController {

	@Autowired
	private UserRoleService userRoleService;

	@PostMapping("assign")
	public ResponseDto<Void> assignRoles(@RequestBody @Valid AssignUserRoleDto assignUserRoleDto) {

		log.info("Received request to update roles of user: " + assignUserRoleDto.getUserId());

		userRoleService.assignRoles(assignUserRoleDto.getUserId(), assignUserRoleDto.getRoleIds());

		return ResponseDto.success("Successfully updated user roles");
	}

	@GetMapping("assigned/{userId}")
	public ResponseDto<List<RoleMetadataDto>> getAssignedRoles(
			@PathVariable(name = "userId") @NotBlank(message = "User Id is mandatory to get roles") String userId) {

		List<RoleMetadataDto> roleMetadataDtos = userRoleService.getRolesAssignedToUser(userId);

		return ResponseDto.success(roleMetadataDtos.size() + " Roles assigned to user", roleMetadataDtos);
	}
}