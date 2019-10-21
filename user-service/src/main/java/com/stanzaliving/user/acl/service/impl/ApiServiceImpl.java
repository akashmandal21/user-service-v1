/**
 * 
 */
package com.stanzaliving.user.acl.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.user.acl.dto.ApiDto;
import com.stanzaliving.core.user.acl.request.dto.AddApiRequestDto;
import com.stanzaliving.user.acl.adapters.ApiAdapter;
import com.stanzaliving.user.acl.db.service.ApiDbService;
import com.stanzaliving.user.acl.entity.ApiEntity;
import com.stanzaliving.user.acl.service.ApiService;

import lombok.extern.log4j.Log4j;

/**
 * @author naveen
 *
 * @date 21-Oct-2019
 */
@Log4j
@Service
public class ApiServiceImpl implements ApiService {

	@Autowired
	private ApiDbService apiDbService;

	@Override
	public ApiDto addApi(AddApiRequestDto addApiRequestDto) {

		if (apiDbService.isActionPresent(addApiRequestDto.getActionUrl())) {
			throw new StanzaException("API already exists with given URL");
		}

		log.info("Adding New API with URL: " + addApiRequestDto.getActionUrl() + " and name: " + addApiRequestDto.getApiName());

		ApiEntity apiEntity = ApiAdapter.getEntityFromRequest(addApiRequestDto);

		apiEntity = apiDbService.save(apiEntity);

		return ApiAdapter.getDto(apiEntity);
	}
}