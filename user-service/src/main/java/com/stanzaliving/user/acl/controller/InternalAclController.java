package com.stanzaliving.user.acl.controller;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleNameUrlExpandedDto;
import com.stanzaliving.user.acl.service.AclService;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/internal/acl/")
public class InternalAclController {

    @Autowired
    private AclService aclService;

    @GetMapping("user/fe/{email}")
    public ResponseDto<List<UserDeptLevelRoleNameUrlExpandedDto>> getUserRolesFe(@PathVariable @NotBlank(message = "User email must not be blank") String email) {

        log.info("Request received to getUserRolesFe for user : " + email);
        return ResponseDto.success(aclService.getUserDeptLevelRoleNameUrlExpandedDtoFeFromEmail(email));

    }
    @PostMapping("user/getList")
    public ResponseDto<String> getList(@RequestBody Map<String,Object> jsonObject) throws IOException {
        List<String> roleUuids = (List<String>) jsonObject.get("roleUuids");
        List<String> accessLevels = (List<String>)jsonObject.get("accessLevels");
        List<String> departments = (List<String>)jsonObject.get("departments");
        String userUuids = jsonObject.get("userUuids").toString();
        List<String> mmIds = (List<String>)jsonObject.get("mmIds");
        return ResponseDto.success(aclService.make_list(roleUuids,accessLevels,departments,userUuids,mmIds));
    }
}
