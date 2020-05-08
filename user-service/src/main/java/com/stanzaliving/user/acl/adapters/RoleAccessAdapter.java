package com.stanzaliving.user.acl.adapters;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.acl.dto.RoleAccessDto;
import com.stanzaliving.core.user.enums.EnumListing;
import com.stanzaliving.user.acl.entity.RoleAccessEntity;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

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

    public List<EnumListing> getAccessLevelEnumAsEnumListing(){
        List<EnumListing> data = new ArrayList<>();
        for (AccessLevel accessLevel: AccessLevel.values()) {
            data.add(
                    EnumListing.builder()
                            .key(accessLevel.name())
                            .value(String.valueOf(accessLevel.getLevelNum()))
                            .build()
            );
        }
        return data;
    }

}
