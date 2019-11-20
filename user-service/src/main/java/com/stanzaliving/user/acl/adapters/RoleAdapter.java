/**
 * 
 */
package com.stanzaliving.user.acl.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.user.acl.entity.RoleEntity;

import lombok.experimental.UtilityClass;

/**
 * @author naveen.kumar
 *
 * @date 22-Oct-2019
 *
 **/
@UtilityClass
public class RoleAdapter {

	public static List<RoleDto> getDto(List<RoleEntity> roleEntities) {

		if (CollectionUtils.isEmpty(roleEntities)) {
			return new ArrayList<>();
		}

		return roleEntities.stream().map(RoleAdapter::getDto).collect(Collectors.toList());

	}

	public static RoleDto getDto(RoleEntity roleEntity) {

		return RoleDto.builder()
				.uuid(roleEntity.getUuid())
				.createdAt(roleEntity.getCreatedAt())
				.createdBy(roleEntity.getCreatedBy())
				.updatedAt(roleEntity.getUpdatedAt())
				.updatedBy(roleEntity.getUpdatedBy())
				.status(roleEntity.isStatus())
				.roleName(roleEntity.getRoleName())
				//TODO: update below
				//.apis(ApiAdapter.getDto(roleEntity.getApiEntities()))
				.build();

	}
}