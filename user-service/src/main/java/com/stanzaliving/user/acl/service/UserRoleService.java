/**
 * 
 */
package com.stanzaliving.user.acl.service;

import java.util.List;

import com.stanzaliving.core.user.acl.dto.RoleMetadataDto;

/**
 * @author naveen.kumar
 *
 * @date 23-Oct-2019
 *
 **/
public interface UserRoleService {

	void assignRoles(String userId, List<String> roleIds);

	List<RoleMetadataDto> getRolesAssignedToUser(String userId);

}