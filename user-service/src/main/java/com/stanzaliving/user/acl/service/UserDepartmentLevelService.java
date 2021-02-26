package com.stanzaliving.user.acl.service;

import java.util.List;
import java.util.Set;

import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;

public interface UserDepartmentLevelService {
    UserDepartmentLevelEntity add(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto);

    void delete(UserDepartmentLevelEntity userDepartmentLevelEntity);

    void revokeAccessLevelEntityForDepartmentOfLevel(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto);

	UserDepartmentLevelEntity findByUuid(String userDepartmentLevelUuid);

	List<UserDepartmentLevelEntity> findByUserUuidAndUuidIn(String userUuid, Set<String> departmentUuids);
}
