package com.stanzaliving.user.acl.controller;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleRequestDto;
import com.stanzaliving.user.acl.service.AclUserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("acl/user")
public class AclUserController {

    @Autowired
    AclUserService aclUserService;

    @PostMapping("addRole")
    public ResponseDto addRole(@RequestBody @Valid AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleDto) {
        log.info("Received request to add role " + addUserDeptLevelRoleDto);
        aclUserService.addRole(addUserDeptLevelRoleDto);
        return ResponseDto.success("Role Assignment successful");
    }

    @PostMapping("revokeAllRolesForDepartment")
    public ResponseDto revokeAllRolesOfDepartment(@RequestParam String userUuid,
                                                  @RequestParam Department department) {
        log.info("Received request to revoke all roles for user {} of department {}", userUuid, department);
        aclUserService.revokeAllRolesOfDepartment(userUuid, department);
        return ResponseDto.success("Role Revocation successful");
    }

    //this api will give only top level roles
    @GetMapping("{userUuid}")
    public ResponseDto<List<UserDeptLevelRoleDto>> getUserRoles(@PathVariable @NotBlank(message = "User uuid must not be blank") String userUuid) {

        log.info("Fetching user role with id: " + userUuid);
        return ResponseDto.success("User roles fetched for user " + userUuid, aclUserService.getUserDeptLevelRole(userUuid));

    }

    //Api's will be needed for adding or removing entity for a particular level on a user
    //Api's will be needed for adding or removing roles for a particular level on a user

}
