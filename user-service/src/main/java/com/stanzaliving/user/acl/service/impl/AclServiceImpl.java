/**
 * 
 */
package com.stanzaliving.user.acl.service.impl;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.user.acl.db.service.impl.UserDepartmentLevelDbServiceImpl;
import com.stanzaliving.user.acl.db.service.impl.UserDepartmentLevelRoleDbServiceImpl;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import com.stanzaliving.user.acl.service.AclService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author naveen.kumar
 *
 * @date 23-Oct-2019
 *
 **/
@Log4j2
@Service
public class AclServiceImpl implements AclService {

	@Autowired
	UserDepartmentLevelDbServiceImpl userDepartmentLevelDbService;

	@Autowired
	UserDepartmentLevelRoleDbServiceImpl userDepartmentLevelRoleDbService;

	@Override
	public boolean isAccesible(String userId, String url) {

		return false;
	}

    public ResponseDto<Map<Department, List<String>>> getDepartmentRoleMap(String userUuid) {

		HashMap<Department, List<String>> departmentRoleMap = new HashMap<>();
		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList;


		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuidAndStatus(userUuid, true);
		for(UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
			userDepartmentLevelRoleEntityList = userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuidAndStatus(userDepartmentLevelEntity.getUuid(), true);
		}
		return null;
	}
}