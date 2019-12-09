package com.stanzaliving.user.acl.service.impl;

import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.user.acl.adapters.UserDepartmentLevelAdapter;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelRoleDbService;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import com.stanzaliving.user.acl.service.UserDepartmentLevelService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Log4j2
@Service
public class UserDepartmentLevelServiceImpl implements UserDepartmentLevelService {

    @Autowired
    UserDepartmentLevelDbService userDepartmentLevelDbService;

    @Autowired
    UserDepartmentLevelRoleDbService userDepartmentLevelRoleDbService;

    @Override
    public UserDepartmentLevelEntity add(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto) {
        UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelDbService.findByUserUuidAndDepartmentAndAccessLevelAndStatus(addUserDeptLevelRequestDto.getUserUuid(), addUserDeptLevelRequestDto.getDepartment(), addUserDeptLevelRequestDto.getAccessLevel(), true);
        if (null != userDepartmentLevelEntity) {
            TreeSet<String> accessLevelEntityListUuid = Arrays.asList(userDepartmentLevelEntity.getCsvAccessLevelEntityUuid().split("\\s*,\\s*")).stream().collect(Collectors.toCollection(TreeSet::new));
            accessLevelEntityListUuid.addAll(addUserDeptLevelRequestDto.getAccessLevelEntityListUuid());
            userDepartmentLevelEntity.setCsvAccessLevelEntityUuid(String.join(",",accessLevelEntityListUuid));
        } else  {
            userDepartmentLevelEntity = UserDepartmentLevelAdapter.getEntityFromRequest(addUserDeptLevelRequestDto);
        }
        return userDepartmentLevelDbService.save(userDepartmentLevelEntity);
    }

    @Override
    public void delete(UserDepartmentLevelEntity userDepartmentLevelEntity) {
        List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuid(userDepartmentLevelEntity.getUuid());
        log.info("Deleting userDepartmentLevelRoleEntityList " + userDepartmentLevelRoleEntityList);
        userDepartmentLevelRoleDbService.delete(userDepartmentLevelRoleEntityList);

        log.info("Deleting userDepartmentLevelEntity " + userDepartmentLevelEntity);
        userDepartmentLevelDbService.delete(userDepartmentLevelEntity);
    }

    @Override
    public void revokeAccessLevelEntityForDepartmentOfLevel(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto) {
        UserDepartmentLevelEntity userDepartmentLevelEntity =
                userDepartmentLevelDbService.findByUserUuidAndDepartmentAndAccessLevelAndStatus(
                        addUserDeptLevelRequestDto.getUserUuid(), addUserDeptLevelRequestDto.getDepartment(), addUserDeptLevelRequestDto.getAccessLevel(), true);

        if (StringUtils.isBlank(userDepartmentLevelEntity.getCsvAccessLevelEntityUuid())) {
            this.delete(userDepartmentLevelEntity);
            throw new StanzaException("user doesn't have access to any entity for " + addUserDeptLevelRequestDto);
        }

        List<String> accessLevelEntityUuidList = Arrays.asList(userDepartmentLevelEntity.getCsvAccessLevelEntityUuid().split("\\s*,\\s*"));
        accessLevelEntityUuidList.removeAll(addUserDeptLevelRequestDto.getAccessLevelEntityListUuid());

        if (CollectionUtils.isEmpty(accessLevelEntityUuidList)) {
            this.delete(userDepartmentLevelEntity);
        } else {
            userDepartmentLevelEntity.setCsvAccessLevelEntityUuid(StringUtils.join(accessLevelEntityUuidList, ","));
            userDepartmentLevelDbService.save(userDepartmentLevelEntity);
        }

    }
}
