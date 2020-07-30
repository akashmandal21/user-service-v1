package com.stanzaliving.user.service;

import com.google.api.services.directory.model.User;
import com.stanzaliving.core.user.dto.UserListAndStatusDto;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface GSuiteService {
	List<User> getUsers();

	UserListAndStatusDto getSegregatedUsers();
}
