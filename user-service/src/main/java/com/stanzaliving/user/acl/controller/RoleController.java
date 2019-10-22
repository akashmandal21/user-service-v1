/**
 * 
 */
package com.stanzaliving.user.acl.controller;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.dto.RoleMetadataDto;
import com.stanzaliving.core.user.acl.request.dto.AddRoleRequestDto;
import com.stanzaliving.core.user.acl.request.dto.UpdateRoleRequestDto;
import com.stanzaliving.user.acl.service.RoleService;

import lombok.extern.log4j.Log4j;

/**
 * @author naveen.kumar
 *
 * @date 22-Oct-2019
 * 
 **/
@Log4j
@RestController
@RequestMapping("acl/role")
public class RoleController {

	@Autowired
	private RoleService roleService;

	@PostMapping("add")
	public ResponseDto<RoleDto> addRole(@RequestBody @Valid AddRoleRequestDto addRoleRequestDto) {

		log.info("Received request to add new role: " + addRoleRequestDto.getRoleName());

		return ResponseDto.success("Added New Role: " + addRoleRequestDto.getRoleName(), roleService.addRole(addRoleRequestDto));
	}

	@PostMapping("update")
	public ResponseDto<RoleDto> updateRole(@RequestBody @Valid UpdateRoleRequestDto updateRoleRequestDto) {

		log.info("Received request to update role: " + updateRoleRequestDto.getRoleId());

		return ResponseDto.success("Updated Role: " + updateRoleRequestDto.getRoleName(), roleService.updateRole(updateRoleRequestDto));
	}

	@GetMapping("names")
	public ResponseDto<List<RoleMetadataDto>> getAllRoleNames() {

		log.info("Received Request to get all role names");

		List<RoleMetadataDto> roleMetadataDtos = roleService.getAllRoleNames();

		return ResponseDto.success("Found " + roleMetadataDtos.size() + " Roles", roleMetadataDtos);
	}

	@GetMapping("{roleId}")
	public ResponseDto<RoleDto> getRole(@PathVariable @NotBlank(message = "Role Id must not be blank") String roleId) {

		log.info("Fetching role with id: " + roleId);

		return ResponseDto.success("Found Role with Id: " + roleId, roleService.getRoleById(roleId));
	}

	@GetMapping("search/{pageNo}/{limit}")
	public ResponseDto<PageResponse<RoleDto>> searchRole(
			@PathVariable(name = "pageNo") @Min(value = 1, message = "Page No must be greater than 0") int pageNo,
			@PathVariable(name = "limit") @Min(value = 1, message = "Limit must be greater than 0") int limit,
			@RequestParam(name = "roleName", required = false) String roleName,
			@RequestParam(name = "status", required = false) Boolean status) {

		log.info("Received Api Search Request With Parameters [Page: " + pageNo + ", Limit: " + limit + ", RoleName: " + roleName + ", Status: " + status + "]");

		PageResponse<RoleDto> roleDtos = roleService.searchRole(roleName, status, pageNo, limit);

		return ResponseDto.success("Found " + roleDtos.getRecords() + " Roles for Search Criteria", roleDtos);

	}
}