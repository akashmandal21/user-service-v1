package com.stanzaliving.user.dto.request;

import com.stanzaliving.core.utilservice.annotations.EnsureNumber;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@ToString(callSuper = true)
public class OtpRequestDto extends LoginDto {

    @NotBlank(message = "OTP cannot be blank")
    @Size(min = 4, max = 6, message = "OTP must be of 4-6 charaters")
    @EnsureNumber(message = "OTP must contain only numbers", fieldName = "OTP")
    private String otp;

}
