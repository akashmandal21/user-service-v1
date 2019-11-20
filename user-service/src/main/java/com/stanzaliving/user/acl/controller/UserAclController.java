package com.stanzaliving.user.acl.controller;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.request.dto.UserRoleAssignRequestDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Log4j2
@RestController
@RequestMapping("acl/user")
public class UserAclController {

    @PostMapping("assign")
    public ResponseDto assignRole(@RequestBody @Valid UserRoleAssignRequestDto userRoleAssignRequestDto) {

        return null;
    }

    @GetMapping("{userUuid}")
    public ResponseDto<UserRoleAssignRequestDto> getUserRole(@PathVariable @NotBlank(message = "User uuid must not be blank") String userUuid) {

        log.info("Fetching user role with id: " + userUuid);

        return null;
    }



}
