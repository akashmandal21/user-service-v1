package com.stanzaliving.user.acl.service.impl;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.request.dto.AddRoleRequestDto;
import com.stanzaliving.user.acl.adapters.RoleAdapter;
import com.stanzaliving.user.acl.db.service.RoleDbService;
import com.stanzaliving.user.acl.entity.RoleEntity;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class RoleServiceImpl {

    @Autowired
    RoleDbService roleDbService;

    @Autowired
    RoleAccessServiceImpl roleAccessService;

    public RoleDto addRole(AddRoleRequestDto addRoleRequestDto) {
        if (roleDbService.isRoleExists(addRoleRequestDto.getRoleName(), addRoleRequestDto.getDepartment(), addRoleRequestDto.getAccessLevel())) {
            throw new StanzaException("Role already exists " + addRoleRequestDto);
        }

        RoleEntity parentRoleEntity = roleDbService.findByUuid(addRoleRequestDto.getParentRoleUuid());
        if (null == parentRoleEntity) {
            throw new StanzaException("Parent role doesn't exist " + addRoleRequestDto);
        }

        RoleEntity roleEntity = RoleAdapter.getEntityFromRequest(addRoleRequestDto);

        roleAccessService.assertSameDepartmentAssignment(parentRoleEntity, roleEntity);
        roleAccessService.assertParentChildAssignment(parentRoleEntity, roleEntity);

        roleDbService.save(roleEntity);

        return RoleAdapter.getDto(roleEntity);
    }

    public RoleDto getRoleByUuid(String roleUuid) {
        RoleEntity roleEntity = roleDbService.findByUuid(roleUuid);

        if (null == roleEntity) {
            throw new StanzaException("Unable to find rule by uuid " + roleUuid);
        }

        return RoleAdapter.getDto(roleEntity);
    }

    public List<RoleDto> findByDepartmentAndAccessLevel(Department department, AccessLevel accessLevel) {
        List<RoleEntity> roleEntityList = roleDbService.findByDepartmentAndAccessLevel(department, accessLevel);
        return RoleAdapter.getDtoList(roleEntityList);
    }

}
