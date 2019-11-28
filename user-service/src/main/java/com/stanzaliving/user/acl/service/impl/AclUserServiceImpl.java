package com.stanzaliving.user.acl.service.impl;

import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleRequestDto;
import com.stanzaliving.user.acl.adapters.UserDepartmentLevelRoleAdapter;
import com.stanzaliving.user.acl.db.service.impl.UserDepartmentLevelDbServiceImpl;
import com.stanzaliving.user.acl.db.service.impl.UserDepartmentLevelRoleDbServiceImpl;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class AclUserServiceImpl {

    @Autowired
    UserDepartmentLevelServiceImpl userDepartmentLevelService;

    @Autowired
    UserDepartmentLevelDbServiceImpl userDepartmentLevelDbService;

    @Autowired
    UserDepartmentLevelRoleServiceImpl userDepartmentLevelRoleService;

    @Autowired
    UserDepartmentLevelRoleDbServiceImpl userDepartmentLevelRoleDbService;

    public void addRole(AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleDto) {

        AddUserDeptLevelRequestDto addUserDeptLevelRequestDto = new AddUserDeptLevelRequestDto(addUserDeptLevelRoleDto);

        UserDepartmentLevelEntity  userDepartmentLevelEntity = userDepartmentLevelService.add(addUserDeptLevelRequestDto);

        List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = addUserDeptLevelRoleDto.getRolesUuid().stream()
                .map(roleUuid -> new UserDepartmentLevelRoleEntity(userDepartmentLevelEntity.getUuid(), roleUuid)).collect(Collectors.toList());

        userDepartmentLevelRoleService.saveAll(userDepartmentLevelRoleEntityList);

    }

    public void revokeAllRolesOfDepartment(String userUuid, Department department) {

        UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelDbService.findByUserUuidAndDepartment(userUuid, department);
        if (null == userDepartmentLevelEntity) {
            throw new StanzaException("User doesn't have any access in this department");
        }

        List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuid(userDepartmentLevelEntity.getUuid());
        log.info("Deleting userDepartmentLevelRoleEntityList " + userDepartmentLevelRoleEntityList);
        userDepartmentLevelRoleDbService.delete(userDepartmentLevelRoleEntityList);

        log.info("Deleting userDepartmentLevelEntity " + userDepartmentLevelEntity);
        userDepartmentLevelDbService.delete(userDepartmentLevelEntity);

    }

    public List<UserDeptLevelRoleDto> getUserDeptLevelRole(String userUuid) {
        List<UserDeptLevelRoleDto> userDeptLevelRoleDtoList = new ArrayList<>();
        List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList;

        List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuidAndStatus(userUuid, true);

        for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
            userDepartmentLevelRoleEntityList = userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuidAndStatus(userDepartmentLevelEntity.getUuid(), true);
            userDeptLevelRoleDtoList.add(UserDepartmentLevelRoleAdapter.getUserDeptLevelRoleDto(userDepartmentLevelEntity, userDepartmentLevelRoleEntityList));
        }
        return userDeptLevelRoleDtoList;
    }
}
