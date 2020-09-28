package com.stanzaliving.user.controller.internal;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleNameUrlExpandedDto;
import com.stanzaliving.core.user.dto.SessionRequestDto;
import com.stanzaliving.user.acl.service.AclService;
import com.stanzaliving.user.service.SessionService;
import com.stanzaliving.user.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/internal/session/")
public class InternalSessionController {

    @Autowired
    private SessionService sessionService;

    @PostMapping("create")
    public ResponseDto create(@RequestBody SessionRequestDto sessionRequestDto) {

        log.info("Request received to create session : " + sessionRequestDto);
        sessionService.createSession(sessionRequestDto);
        return ResponseDto.success("Session Created Successfully");

    }

    @PostMapping("remove")
    public ResponseDto remove(@RequestBody SessionRequestDto sessionRequestDto) {

        log.info("Request received to remove session : " + sessionRequestDto);
        sessionService.removeUserSession(sessionRequestDto.getToken());
        return ResponseDto.success("Session Removed Successfully");

    }
}
