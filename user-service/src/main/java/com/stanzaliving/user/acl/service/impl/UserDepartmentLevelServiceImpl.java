package com.stanzaliving.user.acl.service.impl;

import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.user.acl.adapters.UserDepartmentLevelAdapter;
import com.stanzaliving.user.acl.db.service.impl.UserDepartmentLevelDbServiceImpl;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDepartmentLevelServiceImpl {

    @Autowired
    UserDepartmentLevelDbServiceImpl userDepartmentLevelDbService;

    public UserDepartmentLevelEntity add(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto) {
        UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelDbService.findByUserUuidAndDepartmentAndStatus(addUserDeptLevelRequestDto.getUserUuid(), addUserDeptLevelRequestDto.getDepartment(), true);
        if (null != userDepartmentLevelEntity) {
            throw new StanzaException("User already have access level defined in this department, Please use update API " + addUserDeptLevelRequestDto);
        }
        userDepartmentLevelEntity = UserDepartmentLevelAdapter.getEntityFromRequest(addUserDeptLevelRequestDto);
        return userDepartmentLevelDbService.save(userDepartmentLevelEntity);
    }
}
