package com.stanzaliving.user.acl.adapters;

import com.stanzaliving.core.user.acl.dto.RoleAccessDto;
import com.stanzaliving.user.acl.entity.RoleAccessEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RoleAccessAdapter {

    public static RoleAccessDto getDto(RoleAccessEntity roleAccessEntity) {

        return RoleAccessDto.builder()
                .uuid(roleAccessEntity.getUuid())
                .createdAt(roleAccessEntity.getCreatedAt())
                .createdBy(roleAccessEntity.getCreatedBy())
                .updatedAt(roleAccessEntity.getUpdatedAt())
                .updatedBy(roleAccessEntity.getUpdatedBy())
                .status(roleAccessEntity.isStatus())
                .roleUuid(roleAccessEntity.getRoleUuid())
                .accessUuid(roleAccessEntity.getAccessUuid())
                .roleAccessType(roleAccessEntity.getRoleAccessType())
                .build();

    }

}
