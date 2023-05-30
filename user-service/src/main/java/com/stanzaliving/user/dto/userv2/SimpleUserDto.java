package com.stanzaliving.user.dto.userv2;


import com.stanzaliving.boq_service.dto.LabelValueDto;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleUserDto {
    private LabelValueDto<String> labelValueDto;
    private String mobile;
    private String email;
}
