package com.stanzaliving.user.dto.request;

import com.stanzaliving.core.enums.Source;
import com.stanzaliving.core.user.enums.UserType;
import lombok.*;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {

    private String isoCode;

    private String mobile;

    private String employeeCode;

    private UserType userType;

    private Source userSource;
}
