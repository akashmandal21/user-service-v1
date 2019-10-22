/**
 * 
 */
package com.stanzaliving.user.acl.service;

import java.util.List;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.dto.RoleMetadataDto;
import com.stanzaliving.core.user.acl.request.dto.AddRoleRequestDto;
import com.stanzaliving.core.user.acl.request.dto.UpdateRoleRequestDto;

/**
 * @author naveen.kumar
 *
 * @date 22-Oct-2019
 *
 **/
public interface RoleService {

	RoleDto addRole(AddRoleRequestDto addRoleRequestDto);

	RoleDto updateRole(UpdateRoleRequestDto updateRoleRequestDto);

	List<RoleMetadataDto> getAllRoleNames();
	
	RoleDto getRoleById(String roleId);

	PageResponse<RoleDto> searchRole(String roleName, Boolean status, int pageNo, int limit);
}