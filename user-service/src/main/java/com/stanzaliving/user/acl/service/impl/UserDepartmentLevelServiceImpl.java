package com.stanzaliving.user.acl.service.impl;

import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.user.acl.adapters.UserDepartmentLevelAdapter;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelDbService;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.service.UserDepartmentLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDepartmentLevelServiceImpl implements UserDepartmentLevelService {

    @Autowired
    UserDepartmentLevelDbService userDepartmentLevelDbService;

    @Override
    public UserDepartmentLevelEntity add(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto) {
        UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelDbService.findByUserUuidAndDepartmentAndStatus(addUserDeptLevelRequestDto.getUserUuid(), addUserDeptLevelRequestDto.getDepartment(), true);
        if (null != userDepartmentLevelEntity) {
            throw new StanzaException("User already have access level defined in this department, " +
                    "Please add role after revoking access, current level " + userDepartmentLevelEntity.getAccessLevel() +
                    " in Department " + userDepartmentLevelEntity.getDepartment());
        }
        userDepartmentLevelEntity = UserDepartmentLevelAdapter.getEntityFromRequest(addUserDeptLevelRequestDto);
        return userDepartmentLevelDbService.save(userDepartmentLevelEntity);
    }
}
