package com.stanzaliving.user.controller;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.constants.SecurityConstants;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.user.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/internal/")
public class InternalController {

    @Autowired
    private UserService userService;

    @GetMapping("details/{userUuid}")
    public ResponseDto<UserProfileDto> getUser(@PathVariable String userUuid) {

        log.info("Fetching User with userUuid: " + userUuid);

        return ResponseDto.success("Found User for userUuid", userService.getActiveUserByUserId(userUuid));
    }
}
