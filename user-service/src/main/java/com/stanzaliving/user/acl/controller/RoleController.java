/**
 * 
 */
package com.stanzaliving.user.acl.controller;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.acl.dto.RoleAccessDto;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.request.dto.AddRoleAccessDto;
import com.stanzaliving.core.user.acl.request.dto.AddRoleRequestDto;
import com.stanzaliving.core.user.acl.request.dto.UpdateRoleAccessDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.user.acl.service.RoleAccessService;
import com.stanzaliving.user.acl.service.RoleService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
	RoleService roleService;

	@Autowired
	RoleAccessService roleAccessService;

	@PostMapping("add")
	public ResponseDto<RoleDto> addRole(@RequestBody @Valid AddRoleRequestDto addRoleRequestDto) {

		log.info("Received request to add new role: " + addRoleRequestDto);

		return ResponseDto.success("Added New Role: " + addRoleRequestDto.getRoleName(), roleService.addRole(addRoleRequestDto));
	}

	@GetMapping("{roleUuid}")
	public ResponseDto<RoleDto> getRole(@PathVariable @NotBlank(message = "Role Id must not be blank") String roleUuid) {

		log.info("Fetching role with id: " + roleUuid);

		return ResponseDto.success("Found Role with Id: " + roleUuid, roleService.getRoleByUuid(roleUuid));
	}

	@GetMapping("getRoles")
	public ResponseDto<List<RoleDto>> getRoleByDepartmentAndLevel(
			@RequestParam(name = "department") Department department,
			@RequestParam(name = "accessLevel") AccessLevel accessLevel
	) {
		log.info("Fetching roles by Department {} And Level {} ",department, accessLevel);

		return ResponseDto.success("Found Role with Department: " + department + " level: " + accessLevel,  roleService.findByDepartmentAndAccessLevel(department, accessLevel));
	}


	@GetMapping("list")
	public ResponseDto<List<RoleDto>> filterRoles(
			@RequestParam(name = "department", required = false) Department department,
			@RequestParam(name = "accessLevel", required = false) AccessLevel accessLevel,
			@RequestParam(name = "roleName", required = false) String roleName,
			@RequestParam(name = "userType", required = false) UserType userType
	) {
		log.info("Fetching roles by Department {} And Level {} And roleName And userType",department, accessLevel, roleName, userType);
		return ResponseDto.success("Found Roles with Department: " + department + " level: " + accessLevel,  roleService.filter(roleName, department, accessLevel));
	}


	@PostMapping("addRoleAccess")
	public ResponseDto<RoleAccessDto> addRoleAccess(@RequestBody @Valid AddRoleAccessDto addRoleAccessDto) {

		log.info("Received request for role assignment : " + addRoleAccessDto);

		return ResponseDto.success("Added role access successfully",  roleAccessService.addRoleAccess(addRoleAccessDto));

	}

	@PostMapping("revokeRoleAccess")
	public ResponseDto<Void> revokeRole(@RequestBody @Valid AddRoleAccessDto addRoleAccessDto) {
		log.info("Received request to revoke role assignment : " + addRoleAccessDto);
		roleAccessService.revokeRoleAccess(addRoleAccessDto);
		return ResponseDto.success("Role access revocation successful");
	}

	@PostMapping("updateRoleAccess")
	public ResponseDto<RoleAccessDto> updateRoleAccess(@RequestBody @Valid UpdateRoleAccessDto updateRoleAccessDto) {
		log.info("Received request to update role: " + updateRoleAccessDto);
		return ResponseDto.success("Updated Role: " + updateRoleAccessDto.getRoleAccessUuid(), roleAccessService.updateRoleAccess(updateRoleAccessDto));
	}

}