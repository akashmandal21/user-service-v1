package com.stanzaliving.user.acl.controller;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleListDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleByEmailRequestDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleRequestDto;
import com.stanzaliving.user.acl.service.AclUserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("acl/user")
public class AclUserController {

    @Autowired
    AclUserService aclUserService;

    @PostMapping("add/role")
    public ResponseDto<Void> addRole(@RequestBody @Valid AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleDto) {
        log.info("Received request to add role " + addUserDeptLevelRoleDto);
        aclUserService.addRole(addUserDeptLevelRoleDto);
        return ResponseDto.success("Role Assignment successful");
    }

    //this api will give only top level roles
    @GetMapping("{userUuid}")
    public ResponseDto<List<UserDeptLevelRoleDto>> getUserRoles(@PathVariable @NotBlank(message = "User uuid must not be blank") String userUuid) {

        log.info("Fetching user role with id: " + userUuid);
        return ResponseDto.success("User roles fetched for user " + userUuid, aclUserService.getActiveUserDeptLevelRole(userUuid));

    }

    @PostMapping("revoke/department/roles/all")
    public ResponseDto<Void> revokeAllRolesForDepartment(@RequestParam String userUuid,
                                                  @RequestParam Department department) {
        log.info("Received request to revoke all roles for user {} of department {}", userUuid, department);
        aclUserService.revokeAllRolesOfDepartment(userUuid, department);
        return ResponseDto.success("Role Revocation successful");
    }

    @PostMapping("revoke/department/level/roles/all")
    public ResponseDto<Void> revokeAllRolesForDepartmentOfLevel(@RequestParam @NotEmpty String userUuid,
                                                  @RequestParam @NotNull Department department,
                                                  @RequestParam @NotNull AccessLevel accessLevel) {
        log.info("Received request to revoke all roles for user {} of department {} of level {}", userUuid, department, accessLevel);
        aclUserService.revokeAllRolesOfDepartmentOfLevel(userUuid, department, accessLevel);
        return ResponseDto.success("Role Revocation successful");
    }

    @PostMapping("revoke/department/level/levelEntityList")
    public ResponseDto<Void> revokeAccessLevelEntityForDepartmentOfLevel(@RequestBody @Valid AddUserDeptLevelRequestDto addUserDeptLevelRequestDto) {
        log.info("Received request to revoke Access Level Entity List for user " + addUserDeptLevelRequestDto);
        aclUserService.revokeAccessLevelEntityForDepartmentOfLevel(addUserDeptLevelRequestDto);
        return ResponseDto.success("Access Level Entity Revocation successful");
    }

    @PostMapping("revoke/department/level/roleList")
    public ResponseDto<Void> revokeRolesForDepartmentOfLevel(@RequestBody @Valid UserDeptLevelRoleListDto userDeptLevelRoleListDto) {
        log.info("Received request to revoke role list for user " + userDeptLevelRoleListDto);
        aclUserService.revokeRolesForDepartmentOfLevel(userDeptLevelRoleListDto);
        return ResponseDto.success("Roles Revocation successful");
    }

    @PostMapping("/bulk/add/role")
    public ResponseDto<Void> bulkAddRole(@RequestBody @Valid AddUserDeptLevelRoleByEmailRequestDto addUserDeptLevelRoleByEmailRequestDto) {
        log.info("Received request to add role " + addUserDeptLevelRoleByEmailRequestDto);
        aclUserService.bulkAddRole(addUserDeptLevelRoleByEmailRequestDto);
        return ResponseDto.success("Bulk Role Assignment successful");
    }

}
