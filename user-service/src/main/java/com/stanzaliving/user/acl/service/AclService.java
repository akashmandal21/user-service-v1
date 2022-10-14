package com.stanzaliving.user.acl.service;

import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleNameUrlExpandedDto;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface AclService {
    boolean isAccessible(String userId, String url);

    List<UserDeptLevelRoleNameUrlExpandedDto> getUserDeptLevelRoleNameUrlExpandedDtoFe(String userUuid);

    List<UserDeptLevelRoleNameUrlExpandedDto> getUserDeptLevelRoleNameUrlExpandedDtoBe(String userUuid);

    Set<String> getAccessibleUrlList(String userUuid);

    List<UserDeptLevelRoleNameUrlExpandedDto> getUserDeptLevelRoleNameUrlExpandedDtoFeFromEmail(String email);

    String make_list(List<String> roleUuids, List<String> accessLevels, List<String> departments, String userUuids, List<String> mmIds) throws IOException;
}
