package com.stanzaliving.user.feignclient;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.user.dto.userv2.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "IdentityHttpService", url = "${service.identity.url}")
public interface IdentityHttpService {

    @GetMapping(value = "/internal/redirect/mobile/{mobileNumber}")
    ResponseDto<UserDto> getActiveUserForMobileNumber(@PathVariable Long mobileNumber);

}
