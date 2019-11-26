/**
 * 
 */
package com.stanzaliving.user.acl.controller;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.acl.dto.RoleAssignDto;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.request.dto.AddRoleAssignDto;
import com.stanzaliving.core.user.acl.request.dto.AddRoleRequestDto;
import com.stanzaliving.core.user.acl.request.dto.UpdateRoleRequestDto;
import com.stanzaliving.user.acl.service.impl.RoleServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author naveen.kumar
 *
 * @date 22-Oct-2019
 * 
 **/
@Log4j2
@RestController
@RequestMapping("acl/role")
public class RoleController {

	@Autowired
	RoleServiceImpl roleService;

	@PostMapping("add")
	public ResponseDto<RoleDto> addRole(@RequestBody @Valid AddRoleRequestDto addRoleRequestDto) {

		log.info("Received request to add new role: " + addRoleRequestDto);



		//TODO: update below
		//return ResponseDto.success("Added New Role: " + addRoleRequestDto, roleService.addRole(addRoleRequestDto));
		return null;
	}

	@PostMapping("update")
	public ResponseDto<RoleDto> updateRole(@RequestBody @Valid UpdateRoleRequestDto updateRoleRequestDto) {

		log.info("Received request to update role: " + updateRoleRequestDto.getRoleUuid());

		//TODO: update below
		//return ResponseDto.success("Updated Role: " + updateRoleRequestDto.getRoleName(), roleService.updateRole(updateRoleRequestDto));roleService.addRole(addRoleRequestDto);roleService.updateRole(updateRoleRequestDto);
		return null;
	}

	@GetMapping("{roleId}")
	public ResponseDto<RoleDto> getRole(@PathVariable @NotBlank(message = "Role Id must not be blank") String roleUuid) {

		log.info("Fetching role with id: " + roleUuid);

		//TODO: update below
		return null;
		//return ResponseDto.success("Found Role with Id: " + roleId, roleService.getRoleById(roleId));
	}

	@GetMapping("getRoles")
	public ResponseDto<List<RoleDto>> getRoleByDepartmentAndLevel(
			@RequestParam(name = "department", required = false) Department department,
			@RequestParam(name = "accessLevel", required = false) AccessLevel accessLevel
	) {
		//TODO: update below
		return null;
	}


	@PostMapping("assign")
	public ResponseDto<RoleAssignDto> assignRole(@RequestBody @Valid AddRoleAssignDto addRoleAssignDto) {

		return null;
	}

	@PostMapping("assignBulk")
	public ResponseDto<List<RoleAssignDto>> assignMultipleRoles(@RequestBody @Valid List<AddRoleAssignDto> addRoleAssignDtoList) {
		return null;
	}

	@GetMapping("search/{pageNo}/{limit}")
	public ResponseDto<PageResponse<RoleDto>> searchRole(
			@PathVariable(name = "pageNo") @Min(value = 1, message = "Page No must be greater than 0") int pageNo,
			@PathVariable(name = "limit") @Min(value = 1, message = "Limit must be greater than 0") int limit,
			@RequestParam(name = "status", required = false) Boolean status) {

		log.info("Received Api Search Request With Parameters [Page: " + pageNo + ", Limit: " + limit + ", Status: " + status + "]");

		//PageResponse<RoleDto> roleDtos = roleService.searchRole(roleName, status, pageNo, limit);
		//return ResponseDto.success("Found " + roleDtos.getRecords() + " Roles for Search Criteria", roleDtos);
		return null;
	}

}