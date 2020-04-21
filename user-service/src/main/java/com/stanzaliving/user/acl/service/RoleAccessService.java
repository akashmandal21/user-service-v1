package com.stanzaliving.user.acl.service;

import com.stanzaliving.core.user.acl.dto.RoleAccessDto;
import com.stanzaliving.core.user.acl.request.dto.AddRoleAccessDto;
import com.stanzaliving.core.user.acl.request.dto.UpdateRoleAccessDto;
import com.stanzaliving.core.user.enums.EnumListing;
import com.stanzaliving.user.acl.entity.RoleEntity;

import java.util.List;

public interface RoleAccessService {
	RoleAccessDto addRoleAccess(AddRoleAccessDto addRoleAccessDto);

	void revokeRoleAccess(AddRoleAccessDto addRoleAccessDto);

	RoleAccessDto updateRoleAccess(UpdateRoleAccessDto updateRoleAccessDto);

	void assertSameDepartmentAssignment(RoleEntity roleEntity1, RoleEntity roleEntity2);

	void assertParentChildAssignment(RoleEntity parentRoleEntity, RoleEntity childRoleEntity);

	List<EnumListing> getAccessLevelList();
}
