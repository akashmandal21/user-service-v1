package com.stanzaliving.user.service.impl;

import com.stanzaliving.core.user.dto.UserListAndStatusDto;
import com.stanzaliving.core.user.dto.client.response.GSuiteUserListResponseDto;
import com.stanzaliving.core.user.dto.client.response.GSuiteUserResponseDto;
import com.stanzaliving.user.service.GSuiteUserSyncService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author piyush srivastava "piyush.srivastava@stanzaliving.com"
 *
 * @date 30-July-2020
 */

@Log4j2
@Service
public class GSuiteUserSyncServiceImpl implements GSuiteUserSyncService {


	private GSuiteUserListResponseDto getUsersFromGoogle(String nextPageToken) {
		return GSuiteUserListResponseDto.builder().build();
	}


	@Override
	public UserListAndStatusDto getSegregatedUsers() {
		GSuiteUserListResponseDto gSuiteUserListResponseDto = getUsersFromGoogle(null);

		String nextPageToken = gSuiteUserListResponseDto.getNextPageToken();

		Set<String> activeUsers = new HashSet<>();

		Set<String> inActiveUsers = new HashSet<>();

		do {
			List<GSuiteUserResponseDto> users = gSuiteUserListResponseDto.getUsers();

			if (CollectionUtils.isNotEmpty(users)) {
				users.forEach(user -> {
					if (user.isArchived() || user.isSuspended()) {
						activeUsers.add(user.getPrimaryEmail());
					} else {
						inActiveUsers.add(user.getPrimaryEmail());
					}
				});
			}

		} while (StringUtils.isNotBlank(nextPageToken));

		return UserListAndStatusDto.builder()
				.activeUsers(activeUsers)
				.inActivesUsers(inActiveUsers)
				.build();
	}
}
