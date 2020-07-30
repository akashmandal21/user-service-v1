package com.stanzaliving.user.service;

import com.stanzaliving.core.user.dto.UserListAndStatusDto;
import com.stanzaliving.core.user.dto.client.response.GSuiteUserListResponseDto;

import java.util.Collection;

/**
 * @author piyush srivastava "piyush.srivastava@stanzaliving.com"
 *
 * @date 30-July-2020
 */


public interface GSuiteUserSyncService {
	UserListAndStatusDto getSegregatedUsers();
}
