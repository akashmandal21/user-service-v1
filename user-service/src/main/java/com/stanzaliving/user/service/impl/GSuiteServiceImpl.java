package com.stanzaliving.user.service.impl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Member;
import com.google.api.services.directory.model.User;
import com.google.api.services.directory.model.Users;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.stanzaliving.core.user.dto.UserListAndStatusDto;
import com.stanzaliving.core.user.dto.client.response.GSuiteUserListResponseDto;
import com.stanzaliving.core.user.dto.client.response.GSuiteUserResponseDto;
import com.stanzaliving.user.service.GSuiteService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
@Log4j2
public class GSuiteServiceImpl implements GSuiteService {


	@Autowired
	private GoogleCredentials googleCredentials;

	@Value("${spring.application.name}")
	private String applicationName;


	/**
	 * @author Piyush Srivastava
	 * @return Dto of List of Active & Active Users
	 */
	@Override
	public UserListAndStatusDto getSegregatedUsers() {

		List<User> users = getUsers();

		Set<String> activeUsers = new HashSet<>();

		Set<String> inActiveUsers = new HashSet<>();

		if (CollectionUtils.isNotEmpty(users)) {
			users.forEach(user -> {
				if (user.getArchived() || user.getSuspended()) {
					activeUsers.add(user.getPrimaryEmail());
				} else {
					inActiveUsers.add(user.getPrimaryEmail());
				}
			});
		}

		return UserListAndStatusDto.builder()
				.activeUsers(activeUsers)
				.inActivesUsers(inActiveUsers)
				.build();
	}


	@Override
	public List<User> getUsers() {

		List<User> userList = new ArrayList<>();

		try {
			Directory directory = getDirectory();

			Directory.Users.List usersRequest = directory.users().list();
			do {

				Users usersResponse = usersRequest.setShowDeleted("true").execute();

				userList.addAll(usersResponse.getUsers());

				usersRequest.setPageToken(usersResponse.getNextPageToken());

			} while (StringUtils.isNotBlank(usersRequest.getPageToken()));

		} catch (GeneralSecurityException | IOException e) {

			log.error("Exception while getting users from google " + e.getMessage(), e);

		}

		return userList;
	}

	private Directory getDirectory() throws GeneralSecurityException, IOException {
		NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		googleCredentials.refreshIfExpired();
		HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(googleCredentials);

		return new Directory.Builder(httpTransport, jsonFactory, requestInitializer)
				.setApplicationName(applicationName)
				.build();
	}

}
