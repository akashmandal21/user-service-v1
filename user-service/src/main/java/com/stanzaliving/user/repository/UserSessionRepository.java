/**
 * 
 */
package com.stanzaliving.user.repository;

import org.springframework.stereotype.Repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.entity.UserSessionEntity;

import java.util.List;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Repository
public interface UserSessionRepository extends AbstractJpaRepository<UserSessionEntity, Long> {

	UserSessionEntity findByToken(String token);

    List<UserSessionEntity> findByUserIdAndBrowserAndStatusAndDeviceNotIn(String userId, String app, boolean status, List<String> allowedDeviceIdList);

    List<UserSessionEntity> findByUserIdAndBrowserAndStatusOrderByIdDesc(String userId, String app, boolean status);

    UserSessionEntity findByTokenAndStatus(String token, boolean status);
}