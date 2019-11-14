/**
 * 
 */
package com.stanzaliving.user.acl.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import com.stanzaliving.core.user.acl.dto.DepartmentDto;
import com.stanzaliving.user.acl.entity.DepartmentEntity;

import lombok.experimental.UtilityClass;

/**
 * @author naveen.kumar
 *
 * @date 13-Nov-2019
 *
 **/
@UtilityClass
public class DepartmentAdapter {

	public List<DepartmentDto> getDto(List<DepartmentEntity> departmentEntities) {

		if (CollectionUtils.isEmpty(departmentEntities)) {
			return new ArrayList<>();
		}

		return departmentEntities.stream().map(DepartmentAdapter::getDto).collect(Collectors.toList());
	}

	public DepartmentDto getDto(DepartmentEntity departmentEntity) {

		return DepartmentDto.builder()
				.uuid(departmentEntity.getUuid())
				.createdAt(departmentEntity.getCreatedAt())
				.createdBy(departmentEntity.getCreatedBy())
				.updatedAt(departmentEntity.getUpdatedAt())
				.updatedBy(departmentEntity.getUpdatedBy())
				.status(departmentEntity.isStatus())
				.departmentName(departmentEntity.getDepartmentName())
				.build();

	}
}