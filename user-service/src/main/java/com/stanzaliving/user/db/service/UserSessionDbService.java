/**
 * 
 */
package com.stanzaliving.user.db.service;

import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.user.entity.UserSessionEntity;

import java.util.List;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface UserSessionDbService extends AbstractJpaService<UserSessionEntity, Long> {

	UserSessionEntity getUserSessionForToken(String token);

	List<UserSessionEntity> findByUserIdAndBrowserAndStatusAndDeviceNotIn(String userId, String app, boolean status, List<String> allowedDeviceIdList);

	List<UserSessionEntity> findByUserIdAndBrowserAndStatusOrderByIdDesc(String userId, String app, boolean status);
}