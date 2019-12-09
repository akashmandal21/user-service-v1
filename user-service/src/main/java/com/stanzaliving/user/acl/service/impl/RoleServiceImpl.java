package com.stanzaliving.user.acl.service.impl;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.enums.RoleAccessType;
import com.stanzaliving.core.user.acl.request.dto.AddRoleAccessDto;
import com.stanzaliving.core.user.acl.request.dto.AddRoleRequestDto;
import com.stanzaliving.user.acl.adapters.RoleAdapter;
import com.stanzaliving.user.acl.db.service.RoleDbService;
import com.stanzaliving.user.acl.entity.RoleEntity;
import com.stanzaliving.user.acl.service.RoleAccessService;
import com.stanzaliving.user.acl.service.RoleService;
import com.stanzaliving.user.kafka.service.KafkaUserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    RoleDbService roleDbService;

    @Autowired
    RoleAccessService roleAccessService;

    @Autowired
    KafkaUserService kafkaUserService;

    private static String PARENT_UUID_TO_SKIP_PARENT_ROLE = "SELF";

    @Override
    public RoleDto addRole(AddRoleRequestDto addRoleRequestDto) {
        if (roleDbService.isRoleExists(addRoleRequestDto.getRoleName(), addRoleRequestDto.getDepartment(), addRoleRequestDto.getAccessLevel())) {
            throw new StanzaException("Role already exists " + addRoleRequestDto);
        }

        RoleEntity parentRoleEntity = roleDbService.findByUuid(addRoleRequestDto.getParentRoleUuid());
        if (!PARENT_UUID_TO_SKIP_PARENT_ROLE.equalsIgnoreCase(addRoleRequestDto.getParentRoleUuid()) && null == parentRoleEntity) {
            throw new StanzaException("Parent role doesn't exist for parentUuid" + addRoleRequestDto.getParentRoleUuid());
        }

        RoleEntity roleEntity = RoleAdapter.getEntityFromRequest(addRoleRequestDto);

        if (!PARENT_UUID_TO_SKIP_PARENT_ROLE.equalsIgnoreCase(addRoleRequestDto.getParentRoleUuid())) {
            roleAccessService.assertSameDepartmentAssignment(parentRoleEntity, roleEntity);
            roleAccessService.assertParentChildAssignment(parentRoleEntity, roleEntity);
        }

        roleDbService.save(roleEntity);

        if (!PARENT_UUID_TO_SKIP_PARENT_ROLE.equalsIgnoreCase(addRoleRequestDto.getParentRoleUuid())) {
            roleAccessService.addRoleAccess(
                    AddRoleAccessDto.builder()
                            .roleUuid(addRoleRequestDto.getParentRoleUuid())
                            .accessUuid(roleEntity.getUuid())
                            .roleAccessType(RoleAccessType.ROLE)
                            .build()
            );
        }
        RoleDto roleDto = RoleAdapter.getDto(roleEntity);
        kafkaUserService.sendNewRoleToKafka(roleDto);
        return roleDto;
    }

    @Override
    public RoleDto getRoleByUuid(String roleUuid) {
        RoleEntity roleEntity = roleDbService.findByUuid(roleUuid);

        if (null == roleEntity) {
            throw new StanzaException("Unable to find rule by uuid " + roleUuid);
        }

        return RoleAdapter.getDto(roleEntity);
    }

    @Override
    public List<RoleDto> findByDepartmentAndAccessLevel(Department department, AccessLevel accessLevel) {
        List<RoleEntity> roleEntityList = roleDbService.findByDepartmentAndAccessLevel(department, accessLevel);
        return RoleAdapter.getDtoList(roleEntityList);
    }

}
