/**
 * 
 */
package com.stanzaliving.user.service.impl;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.user.constants.UserErrorCodes;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.user.db.service.UserSessionDbService;
import com.stanzaliving.user.entity.UserSessionEntity;
import com.stanzaliving.user.exception.AuthException;
import com.stanzaliving.user.service.SessionService;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Log4j2
@Service
public class SessionServiceImpl implements SessionService {

	@Value("${bcrypt.salt}")
	private String bcryptSalt;

	@Autowired
	private UserSessionDbService userSessionDbService;

	@Override
	public UserSessionEntity createUserSession(UserDto userDto, String token) {

		log.info("Creating Session for User: " + userDto.getUuid());

		UserSessionEntity userSessionEntity =
				UserSessionEntity.builder()
						.userId(userDto.getUuid())
						.token(getBcryptPassword(token))
						.userType(userDto.getUserType())
						.build();

		userSessionEntity = userSessionDbService.saveAndFlush(userSessionEntity);

		log.info("Created Session: " + userSessionEntity.getUuid() + " for User: " + userDto.getUuid());

		return userSessionEntity;
	}

	@Override
	public UserSessionEntity getUserSessionByToken(String token) {
		return userSessionDbService.getUserSessionForToken(getBcryptPassword(token));
	}

	@Override
	public void removeUserSession(String token) {

		UserSessionEntity userSessionEntity = getUserSessionByToken(token);

		if (Objects.isNull(userSessionEntity)) {
			throw new AuthException("No User Session Found!! Please Login!!", UserErrorCodes.SESSION_NOT_FOUND);
		}

		log.info("Invalidating User Session: " + userSessionEntity.getUuid() + " for User: " + userSessionEntity.getUserId());

		userSessionEntity.setStatus(false);
		userSessionDbService.updateAndFlush(userSessionEntity);
	}

	@Override
	public UserSessionEntity updateUserSession(UserSessionEntity userSessionEntity) {
		return userSessionDbService.updateAndFlush(userSessionEntity);
	}

	private String getBcryptPassword(String password) {
		return BCrypt.hashpw(password, bcryptSalt);
	}

}