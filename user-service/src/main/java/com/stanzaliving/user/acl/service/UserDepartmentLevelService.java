package com.stanzaliving.user.acl.service;

import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserDepartmentLevelService {
    UserDepartmentLevelEntity add(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto);

    void delete(UserDepartmentLevelEntity userDepartmentLevelEntity);

	void delete(Collection<UserDepartmentLevelEntity> userDepartmentLevelEntityIds);

    void revokeAccessLevelEntityForDepartmentOfLevel(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto);

	UserDepartmentLevelEntity findByUuid(String userDepartmentLevelUuid);
}
