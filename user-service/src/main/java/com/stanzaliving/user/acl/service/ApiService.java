/**
 * 
 */
package com.stanzaliving.user.acl.service;

import com.stanzaliving.core.user.acl.dto.ApiDto;
import com.stanzaliving.core.user.acl.request.dto.AddApiRequestDto;

/**
 * @author naveen
 *
 * @date 21-Oct-2019
 */
public interface ApiService {

	ApiDto addApi(AddApiRequestDto addApiRequestDto);

}