package com.stanzaliving.user.feignclient;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.user.dto.userv2.UserDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Log4j2
public class IdentityFeignService {

    @Autowired
    @Lazy
    private IdentityHttpService identityHttpService;

    public UserDto getActiveUserFromIdentity(Long mobileNumber){
        try {
            ResponseDto<UserDto> userDtoResponseDto = identityHttpService.getActiveUserForMobileNumber(mobileNumber);
            if (Objects.nonNull(userDtoResponseDto) && Objects.nonNull(userDtoResponseDto.getData())) {
                return userDtoResponseDto.getData();
            }
        }
        catch (Exception e){}
        return null;
    }
}
