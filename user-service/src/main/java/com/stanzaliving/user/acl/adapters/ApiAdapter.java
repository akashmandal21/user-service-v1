/**
 * 
 */
package com.stanzaliving.user.acl.adapters;

import com.stanzaliving.core.user.acl.dto.ApiDto;
import com.stanzaliving.core.user.acl.request.dto.AddApiRequestDto;
import com.stanzaliving.user.acl.entity.ApiEntity;

import lombok.experimental.UtilityClass;

/**
 * @author naveen
 *
 * @date 21-Oct-2019
 */
@UtilityClass
public class ApiAdapter {

	public static ApiEntity getEntityFromRequest(AddApiRequestDto addApiRequestDto) {

		return ApiEntity.builder()
				.apiName(addApiRequestDto.getApiName())
				.actionUrl(addApiRequestDto.getActionUrl())
				.category(addApiRequestDto.getCategory())
				.build();
	}

	public static ApiDto getDto(ApiEntity apiEntity) {

		return ApiDto.builder()
				.uuid(apiEntity.getUuid())
				.createdAt(apiEntity.getCreatedAt())
				.createdBy(apiEntity.getCreatedBy())
				.updatedAt(apiEntity.getUpdatedAt())
				.updatedBy(apiEntity.getUpdatedBy())
				.status(apiEntity.isStatus())
				.apiName(apiEntity.getApiName())
				.actionUrl(apiEntity.getActionUrl())
				.category(apiEntity.getCategory())
				.build();
	}
}