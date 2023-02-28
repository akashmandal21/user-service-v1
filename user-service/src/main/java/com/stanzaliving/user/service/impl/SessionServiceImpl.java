/**
 * 
 */
package com.stanzaliving.user.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.base.utils.StanzaUtils;
import com.stanzaliving.core.user.enums.App;
import com.stanzaliving.user.entity.UserAppDeviceConfigEntity;
import com.stanzaliving.user.entity.UserAppSessionConfigEntity;
import com.stanzaliving.user.repository.UserAppDeviceConfigRepository;
import com.stanzaliving.user.repository.UserAppSessionConfigRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.constants.SecurityConstants;
import com.stanzaliving.core.base.exception.AuthException;
import com.stanzaliving.core.user.constants.UserErrorCodes;
import com.stanzaliving.core.user.dto.SessionRequestDto;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.user.db.service.UserSessionDbService;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.entity.UserSessionEntity;
import com.stanzaliving.user.service.SessionService;
import com.stanzaliving.user.service.UserService;

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

	@Autowired
	private UserService userService;

	@Autowired
	private UserAppSessionConfigRepository userAppSessionConfigRepository;

	@Autowired
	private UserAppDeviceConfigRepository userAppDeviceConfigRepository;

	@Value("${login.max.count.SIGMA}")
	int sigmaMaxAllowedSessionsCount;

	@Value("${login.max.count.ALFRED}")
	int alfredMaxAllowedSessionsCount;

	@Value("${login.max.count.NUCLEUS}")
	int nucleusMaxAllowedSessionsCount;

	@Value("${login.max.count.NEXUS}")
	int nexusMaxAllowedSessionsCount;

	@Override
	public UserSessionEntity createUserSession(UserDto userDto, String token, App app, String deviceId) {

		log.info("Creating Session for User: " + userDto.getUuid());

		validateDeviceId(userDto.getUuid(), app, deviceId);

		UserSessionEntity userSessionEntity =
				UserSessionEntity.builder()
						.userId(userDto.getUuid())
						.token(getBcryptPassword(token))
						.userType(userDto.getUserType())
						.browser(Objects.nonNull(app)  ?  app.name() : null)
						.device(deviceId)
						.build();

		userSessionEntity = userSessionDbService.saveAndFlush(userSessionEntity);

		log.info("Created Session: " + userSessionEntity.getUuid() + " for User: " + userDto.getUuid());

		return userSessionEntity;
	}

	@Override
	public UserSessionEntity refreshUserSession(String token, App app, String deviceId) {
		UserSessionEntity userSessionEntity = null;
		try {
			log.info("Request received to refresh user session");

			userSessionEntity = getUserSessionByToken(token);

			if (Objects.isNull(userSessionEntity)) {
				log.error("No User Session Found");
				throw new AuthException("No User Session Found!! Please Login!!", UserErrorCodes.SESSION_NOT_FOUND);
			}

			log.info("User session found for user : {} . Getting active user ...", userSessionEntity.getUserId());

			UserDto user = userService.getActiveUserByUuid(userSessionEntity.getUserId());

			log.info("Refresh User Session: " + userSessionEntity.getUuid() + " for User: " + user.getUuid());

			validateDeviceId(userSessionEntity.getUuid(), app, deviceId);

			String newToken = StanzaUtils.generateUniqueId();

			userSessionEntity.setToken(getBcryptPassword(newToken));
			userSessionEntity.setStatus(true);

			log.info("Updating userSessionEntity {} for user {}", userSessionEntity.getUuid(),
					userSessionEntity.getUserId());
			userSessionEntity = userSessionDbService.updateAndFlush(userSessionEntity);
			log.info("Successfully updated userSessionEntity {} for user {}", userSessionEntity.getUuid(),
					userSessionEntity.getUserId());

			return userSessionEntity;
		} catch (Exception e) {
			String userId = Objects.nonNull(userSessionEntity)? userSessionEntity.getUserId(): StringUtils.EMPTY;
			log.error("Exception while refreshing user session : {} for user : {}", e.getMessage(), userId);
			throw new StanzaException(e);
		}
	}

	@Override
	public UserSessionEntity getUserSessionByToken(String token) {
		log.info("Request received for getUserSessionByToken");
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

    @Override
    public void createSession(SessionRequestDto sessionRequestDto) {
		List<UserEntity> userEntityList = userService.getUserByEmail(sessionRequestDto.getEmail().trim());
		if (CollectionUtils.isEmpty(userEntityList)) {
			log.info("User not found for email: " + sessionRequestDto.getEmail());			//if all users of venta will be created in user service, then throw exception
			return;
		}

		String token = sessionRequestDto.getToken().replace(SecurityConstants.VENTA_TOKEN_PREFIX, "");
		UserSessionEntity userSessionEntity = getUserSessionByToken(token);
		if (null != userSessionEntity) {
			log.info("Session already exist with token {}", token);
			return;
		}

		UserEntity userEntity = userEntityList.get(0);
		userSessionEntity = UserSessionEntity.builder()
						.userId(userEntity.getUuid())
						.token(getBcryptPassword(token))
						.userType(userEntity.getUserType())
						.build();
		userSessionEntity = userSessionDbService.saveAndFlush(userSessionEntity);

		log.info("Created Manual Session: " + userSessionEntity.getUuid() + " for User: " + userEntity.getUuid());
    }

    private String getBcryptPassword(String password) {
		return BCrypt.hashpw(password, bcryptSalt);
	}

	@Override
	public void validateDeviceId(String userId, App app, String deviceId){
		log.info("Inside validateDeviceId for userId: {}, app: {}, userId: {}", userId, app, deviceId);
		if(StringUtils.isBlank(userId) || Objects.isNull(app) || StringUtils.isBlank(deviceId))
			return;

		if(App.appsEligibleForDeviceIdCheck().contains(app)) {
			List<UserAppDeviceConfigEntity> userAppDeviceConfigEntities = Optional.ofNullable(userAppDeviceConfigRepository.findByUserIdAndAppAndStatus(userId, app, true)).orElse(new ArrayList<>());
			List<String> allowedDeviceIdList = Optional.of(userAppDeviceConfigEntities.stream().map(UserAppDeviceConfigEntity::getDeviceId).collect(Collectors.toList())).orElse(new ArrayList<>());

			if(CollectionUtils.isNotEmpty(allowedDeviceIdList)) {
				List<UserSessionEntity> userSessionEntities = Optional.ofNullable(userSessionDbService.findByUserIdAndBrowserAndStatusAndDeviceNotIn(userId, app.name(), true, allowedDeviceIdList)).orElse(new ArrayList<>());
				userSessionEntities.forEach(x -> x.setStatus(false));
				userSessionDbService.saveAndFlush(userSessionEntities);
			}
			if(CollectionUtils.isNotEmpty(allowedDeviceIdList) && !allowedDeviceIdList.contains(deviceId))
				throw new StanzaException("This device is not allowed to login for this user");
		}
	}

	@Override
	public void validatePreviousSessions(String userId, App app, String deviceId){
		log.info("Inside validatePreviousSessions method with userId : {}, app : {}, deviceId : {}", userId, app, deviceId);

		try {
			if (Objects.nonNull(app) && App.appsEligibleForUserSessionCheck().contains(app)) {
				//check if the user exists in user app session config
				int maxAllowedSessionsCount = Optional.ofNullable(userAppSessionConfigRepository.findByUserIdAndAppAndStatus(userId, app, true)).map(UserAppSessionConfigEntity::getMaxLoginAllowed).orElse(checkMaxAllowedCounts(app));

				if(maxAllowedSessionsCount ==0)
					throw new StanzaException("You are not allowed to login");

				List<UserSessionEntity> userSessionEntitiesBasedOnAppName = Optional.ofNullable(userSessionDbService.findByUserIdAndBrowserAndStatusOrderByIdDesc(userId, app.name(), true)).orElse(new ArrayList<>());
				List<UserSessionEntity> userSessionEntities = Optional.ofNullable(userSessionDbService.findByUserIdAndBrowserIsNullAndStatusOrderByIdDesc(userId, true)).orElse(new ArrayList<>());

				if(userSessionEntitiesBasedOnAppName.size() == 0 && userSessionEntities.size() > 0) { //Will be called one time
					if (userSessionEntities.size() <= maxAllowedSessionsCount || maxAllowedSessionsCount == -1)
						return;
					List<UserSessionEntity> sessionsToRemove = Optional.of(userSessionEntities.subList(maxAllowedSessionsCount, userSessionEntities.size())).orElse(new ArrayList<>());
					sessionsToRemove.forEach(x -> x.setStatus(false));
					userSessionDbService.saveAndFlush(sessionsToRemove);
				} else {
					if (userSessionEntitiesBasedOnAppName.size() <= maxAllowedSessionsCount || maxAllowedSessionsCount == -1)
						return;
					List<UserSessionEntity> sessionsToRemove = Optional.of(userSessionEntitiesBasedOnAppName.subList(maxAllowedSessionsCount, userSessionEntitiesBasedOnAppName.size())).orElse(new ArrayList<>());
					sessionsToRemove.forEach(x -> x.setStatus(false));
					userSessionDbService.saveAndFlush(sessionsToRemove);
				}
			}
		}
		catch (StanzaException se){
			log.error(se);
			throw se;
		}
		catch (Exception e){
			log.error("Exception while validating user sessions count, Error is : {}", e.getMessage(), e);
		}
	}

	private int checkMaxAllowedCounts(App app) {
		try {
			if (App.SIGMA.equals(app)) return sigmaMaxAllowedSessionsCount;

			if (App.ALFRED.equals(app)) return alfredMaxAllowedSessionsCount;

			if (App.NUCLEUS.equals(app)) return nucleusMaxAllowedSessionsCount;

			if (App.NEXUS.equals(app)) return nexusMaxAllowedSessionsCount;
		} catch (Exception e) {
			log.error("Exception while fetching the max allowed sessions count from properties, error is : {}", e.getMessage(), e);
		}
		return -1;
	}
}