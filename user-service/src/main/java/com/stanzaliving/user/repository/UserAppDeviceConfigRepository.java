/**
 * @author gaurav.likhar
 * @date 21/02/23
 * @project_name user-service
 **/

package com.stanzaliving.user.repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.entity.UserAppDeviceConfigEntity;

import java.util.List;

public interface UserAppDeviceConfigRepository extends AbstractJpaRepository<UserAppDeviceConfigEntity, Long> {
    List<UserAppDeviceConfigEntity> findByUserIdAndStatus(String userId, boolean status);
}
