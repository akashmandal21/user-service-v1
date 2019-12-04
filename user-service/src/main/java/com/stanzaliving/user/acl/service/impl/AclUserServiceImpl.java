package com.stanzaliving.user.acl.service.impl;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleListDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleRequestDto;
import com.stanzaliving.user.acl.adapters.UserDepartmentLevelRoleAdapter;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelRoleDbService;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import com.stanzaliving.user.acl.service.AclUserService;
import com.stanzaliving.user.acl.service.UserDepartmentLevelRoleService;
import com.stanzaliving.user.acl.service.UserDepartmentLevelService;
import com.stanzaliving.user.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class AclUserServiceImpl implements AclUserService {

    @Autowired
    UserDepartmentLevelService userDepartmentLevelService;

    @Autowired
    UserDepartmentLevelDbService userDepartmentLevelDbService;

    @Autowired
    UserDepartmentLevelRoleService userDepartmentLevelRoleService;

    @Autowired
    UserDepartmentLevelRoleDbService userDepartmentLevelRoleDbService;

    @Autowired
    UserService userService;

    @Override
    public void addRole(AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleDto) {

        userService.assertActiveUserByUserUuid(addUserDeptLevelRoleDto.getUserUuid());

        AddUserDeptLevelRequestDto addUserDeptLevelRequestDto = new AddUserDeptLevelRequestDto(addUserDeptLevelRoleDto);

        UserDepartmentLevelEntity  userDepartmentLevelEntity = userDepartmentLevelService.add(addUserDeptLevelRequestDto);

        List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = userDepartmentLevelRoleService.addRoles(userDepartmentLevelEntity.getUuid(), addUserDeptLevelRoleDto.getRolesUuid());

    }

    @Override
    public void revokeAllRolesOfDepartment(String userUuid, Department department) {

        userService.assertActiveUserByUserUuid(userUuid);

        List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuidAndDepartment(userUuid, department);
        if (CollectionUtils.isEmpty(userDepartmentLevelEntityList)) {
            throw new StanzaException("User doesn't have any access in this department");
        }

        for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
            userDepartmentLevelService.delete(userDepartmentLevelEntity);
        }

        return;

    }

    @Override
    public List<UserDeptLevelRoleDto> getUserDeptLevelRole(String userUuid) {

        userService.assertActiveUserByUserUuid(userUuid);

        List<UserDeptLevelRoleDto> userDeptLevelRoleDtoList = new ArrayList<>();
        List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList;

        List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuidAndStatus(userUuid, true);

        for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
            userDepartmentLevelRoleEntityList = userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuidAndStatus(userDepartmentLevelEntity.getUuid(), true);
            userDeptLevelRoleDtoList.add(UserDepartmentLevelRoleAdapter.getUserDeptLevelRoleDto(userDepartmentLevelEntity, userDepartmentLevelRoleEntityList));
        }
        return userDeptLevelRoleDtoList;
    }

    @Override
    public void revokeAllRolesOfDepartmentOfLevel(String userUuid, Department department, AccessLevel accessLevel) {
        userService.assertActiveUserByUserUuid(userUuid);

        List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuidAndDepartmentAndAccessLevel(userUuid, department, accessLevel);
        if (CollectionUtils.isEmpty(userDepartmentLevelEntityList)) {
            throw new StanzaException("User doesn't have any access in this department");
        }

        for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
            userDepartmentLevelService.delete(userDepartmentLevelEntity);
        }

        return;
    }

    @Override
    public void revokeAccessLevelEntityForDepartmentOfLevel(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto) {

        userService.assertActiveUserByUserUuid(addUserDeptLevelRequestDto.getUserUuid());

        userDepartmentLevelService.revokeAccessLevelEntityForDepartmentOfLevel(addUserDeptLevelRequestDto);

    }

    @Override
    public void revokeRolesForDepartmentOfLevel(UserDeptLevelRoleListDto userDeptLevelRoleListDto) {
        userService.assertActiveUserByUserUuid(userDeptLevelRoleListDto.getUserUuid());

        UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelDbService.findByUserUuidAndDepartmentAndAccessLevelAndStatus
                (userDeptLevelRoleListDto.getUserUuid(), userDeptLevelRoleListDto.getDepartment(), userDeptLevelRoleListDto.getAccessLevel(), true);

        if (null == userDepartmentLevelEntity) {
            throw new StanzaException("Unable to revoke roles, User doesn't exist at this level in the department");
        }

        userDepartmentLevelRoleService.revokeRoles(userDepartmentLevelEntity.getUuid(), userDeptLevelRoleListDto.getRolesUuid());

    }
}
