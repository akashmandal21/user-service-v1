package com.stanzaliving.user.acl.service;

import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;

public interface UserDepartmentLevelService {
    UserDepartmentLevelEntity add(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto);
}
