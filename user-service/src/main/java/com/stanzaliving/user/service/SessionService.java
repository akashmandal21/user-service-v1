/**
 * 
 */
package com.stanzaliving.user.service;

import com.stanzaliving.core.user.dto.SessionRequestDto;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.enums.App;
import com.stanzaliving.user.entity.UserSessionEntity;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface SessionService {

	UserSessionEntity createUserSession(UserDto userDto, String token, App app, String deviceId);

	UserSessionEntity refreshUserSession(String token, App app, String deviceId);

	UserSessionEntity getUserSessionByToken(String token);

	void removeUserSession(String token);

	UserSessionEntity updateUserSession(UserSessionEntity userSessionEntity);

	void createSession(SessionRequestDto sessionRequestDto);

	void validateDeviceId(String userId, App app, String deviceId);

	void validatePreviousSessions(String userId, App app, String deviceId);
}