package com.stanzaliving.user.acl.adapters;


import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleNameUrlExpandedDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class UserDepartmentLevelRoleAdapter {

    public static UserDepartmentLevelEntity getEntityFromRequest(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto) {
        return UserDepartmentLevelEntity.builder()
                .userUuid(addUserDeptLevelRequestDto.getUserUuid())
                .department(addUserDeptLevelRequestDto.getDepartment())
                .accessLevel(addUserDeptLevelRequestDto.getAccessLevel())
                .csvAccessLevelEntityUuid(String.join(",", addUserDeptLevelRequestDto.getAccessLevelEntityListUuid()))
                .build();
    }

    public static UserDeptLevelRoleDto getUserDeptLevelRoleDto(UserDepartmentLevelEntity userDepartmentLevelEntity, List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList) {
        return UserDeptLevelRoleDto.builder()
                .userDeptLevelUuid(userDepartmentLevelEntity.getUuid())
                .userUuid(userDepartmentLevelEntity.getUserUuid())
                .department(userDepartmentLevelEntity.getDepartment())
                .accessLevel(userDepartmentLevelEntity.getAccessLevel())
                .accessLevelEntityListUuid(Arrays.asList(userDepartmentLevelEntity.getCsvAccessLevelEntityUuid().split("\\s*,\\s*")))
                .rolesUuid(userDepartmentLevelRoleEntityList.stream().map(entity -> entity.getRoleUuid()).collect(Collectors.toList()))
                .build();
    }

    public static UserDeptLevelRoleNameUrlExpandedDto getUserDeptLevelRoleNameUrlExpandedDto() {
        return null;
    }
}
