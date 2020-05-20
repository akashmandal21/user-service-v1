package com.stanzaliving.user.controller;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.constants.SecurityConstants;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.user.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

@Log4j2
@RestController
@RequestMapping("/internal/")
public class InternalController {

    @Autowired
    private UserService userService;

    @GetMapping("details")
    public ResponseDto<UserProfileDto> getUser(
            @RequestAttribute(name = SecurityConstants.USER_ID) @NotBlank(message = "User Id is mandatory to get user") String userId) {

        log.info("Fetching User with UserId: " + userId);

        return ResponseDto.success("Found User for User Id", userService.getActiveUserByUserId(userId));
    }
}
