/**
 * @author gaurav.likhar
 * @date 22/02/23
 * @project_name user-service
 **/

package com.stanzaliving.user.repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.core.user.enums.App;
import com.stanzaliving.user.entity.UserAppSessionConfigEntity;

public interface UserAppSessionConfigRepository extends AbstractJpaRepository<UserAppSessionConfigEntity, Long> {
    UserAppSessionConfigEntity findByUserIdAndAppAndStatus(String userId, App app, boolean status);
}
