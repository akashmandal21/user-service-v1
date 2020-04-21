package com.stanzaliving.user.acl.service.impl;

import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.user.acl.dto.RoleAccessDto;
import com.stanzaliving.core.user.acl.enums.RoleAccessType;
import com.stanzaliving.core.user.acl.request.dto.AddRoleAccessDto;
import com.stanzaliving.core.user.acl.request.dto.UpdateRoleAccessDto;
import com.stanzaliving.core.user.enums.EnumListing;
import com.stanzaliving.user.acl.adapters.RoleAccessAdapter;
import com.stanzaliving.user.acl.db.service.ApiDbService;
import com.stanzaliving.user.acl.db.service.RoleAccessDbService;
import com.stanzaliving.user.acl.db.service.RoleDbService;
import com.stanzaliving.user.acl.entity.RoleAccessEntity;
import com.stanzaliving.user.acl.entity.RoleEntity;
import com.stanzaliving.user.acl.service.RoleAccessService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class RoleAccessServiceImpl implements RoleAccessService {

    @Autowired
    RoleDbService roleDbService;

    @Autowired
    ApiDbService apiDbService;

    @Autowired
    RoleAccessDbService roleAccessDbService;

    @Override
    public RoleAccessDto addRoleAccess(AddRoleAccessDto addRoleAccessDto) {

        assertValidRoleAccessRequest(addRoleAccessDto.getRoleUuid(), addRoleAccessDto.getAccessUuid(), addRoleAccessDto.getRoleAccessType());

        RoleAccessEntity roleAccessEntity = roleAccessDbService.findByRoleUuidAndAccessUuidAndRoleAccessType(addRoleAccessDto.getRoleUuid(),
                addRoleAccessDto.getAccessUuid(), addRoleAccessDto.getRoleAccessType());

        if (null != roleAccessEntity) {
            roleAccessEntity.setStatus(true);
        } else {
            roleAccessEntity = new RoleAccessEntity(addRoleAccessDto.getRoleUuid(), addRoleAccessDto.getAccessUuid(), addRoleAccessDto.getRoleAccessType());
        }

        roleAccessDbService.save(roleAccessEntity);

        return RoleAccessAdapter.getDto(roleAccessEntity);
    }

    @Override
    public void revokeRoleAccess(AddRoleAccessDto addRoleAccessDto) {

        assertValidRoleAccessRequest(addRoleAccessDto.getRoleUuid(), addRoleAccessDto.getAccessUuid(), addRoleAccessDto.getRoleAccessType());

        RoleAccessEntity roleAccessEntity = roleAccessDbService.findByRoleUuidAndAccessUuidAndRoleAccessType(addRoleAccessDto.getRoleUuid(),
                addRoleAccessDto.getAccessUuid(), addRoleAccessDto.getRoleAccessType());

        if (null != roleAccessEntity) {
            roleAccessEntity.setStatus(false);
        }

        roleAccessDbService.save(roleAccessEntity);

    }

    @Override
    public RoleAccessDto updateRoleAccess(UpdateRoleAccessDto updateRoleAccessDto) {

        assertValidRoleAccessRequest(updateRoleAccessDto.getRoleUuid(), updateRoleAccessDto.getAccessUuid(), updateRoleAccessDto.getRoleAccessType());

        RoleAccessEntity roleAccessEntity = roleAccessDbService.findByUuid(updateRoleAccessDto.getRoleAccessUuid());

        if (null == roleAccessEntity) {
            throw new StanzaException("Unable to update roleAccess, RoleAccess doesn't exist " + updateRoleAccessDto.getRoleAccessUuid());
        }

        roleAccessEntity.setRoleUuid(updateRoleAccessDto.getRoleUuid());
        roleAccessEntity.setAccessUuid(updateRoleAccessDto.getAccessUuid());
        roleAccessEntity.setRoleAccessType(updateRoleAccessDto.getRoleAccessType());

        roleAccessDbService.save(roleAccessEntity);

        return RoleAccessAdapter.getDto(roleAccessEntity);

    }

    //method will throw StanzaException for invalid requests
    private void assertValidRoleAccessRequest(String roleUuid, String accessUuid, RoleAccessType roleAccessType) {
        RoleEntity roleEntity = roleDbService.findByUuid(roleUuid);
        if (null == roleEntity) {
            throw new StanzaException("Role doesn't exist, roleUuid " + roleUuid);
        }

        if (RoleAccessType.ROLE.equals(roleAccessType)) {
            RoleEntity accessRoleEntity = roleDbService.findByUuid(accessUuid);
            if (null == accessRoleEntity) {
                throw new StanzaException(roleAccessType + " Entity doesn't exist, accessUuid " + accessUuid);
            }
            assertSameDepartmentAssignment(roleEntity, accessRoleEntity);
            assertLowerLevelAssignment(roleEntity, accessRoleEntity);

        } else {
            if (!apiDbService.existsByUuidAndStatus(accessUuid, true)) {
                throw new StanzaException(roleAccessType + " Entity doesn't exist, accessUuid " + accessUuid);
            }
        }
    }

    private void assertLowerLevelAssignment(RoleEntity roleEntity, RoleEntity assignedRoleEntity) {
        if (null == roleEntity || null == assignedRoleEntity) {
            throw new StanzaException("Either of roleEntity or assignedRoleEntity not found " + roleEntity + " " + assignedRoleEntity);
        }

        if (!assignedRoleEntity.getAccessLevel().isLower(roleEntity.getAccessLevel())) {
            throw new StanzaException("Only Role of higher level can be assigned to role of lower level " + roleEntity.getAccessLevel() + " " + assignedRoleEntity.getAccessLevel());
        }
    }

    @Override
    public void assertSameDepartmentAssignment(RoleEntity roleEntity1, RoleEntity roleEntity2) {
        if (null == roleEntity1 || null == roleEntity2) {
            throw new StanzaException("Either of roleEntity1 or roleEntity2 not found " + roleEntity1 + roleEntity2);
        }

        if (!roleEntity1.getDepartment().equals(roleEntity2.getDepartment())) {
            throw new StanzaException("Cross department role to role assignment not allowed " + roleEntity1.getDepartment() + " " + roleEntity2.getDepartment());
        }
    }

    @Override
    public void assertParentChildAssignment(RoleEntity parentRoleEntity, RoleEntity childRoleEntity) {
        if (null == parentRoleEntity || null == childRoleEntity) {
            throw new StanzaException("Either of parentEntity or childEntity not found " + parentRoleEntity + childRoleEntity);
        }

        if (!childRoleEntity.getAccessLevel().isLower(parentRoleEntity.getAccessLevel())) {
            throw new StanzaException("Parent Role should be at higher level than current role, parentUuid " + parentRoleEntity.getUuid() + ", childUuid " + childRoleEntity.getUuid());
        }
    }

    @Override
    public List<EnumListing> getAccessLevelList() {
        return RoleAccessAdapter.getAccessLevelEnumAsEnumListing();
    }
}
