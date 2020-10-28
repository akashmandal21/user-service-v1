/**
 * 
 */
package com.stanzaliving.user.db.service;

import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.core.user.enums.OtpType;
import com.stanzaliving.user.entity.BlacklistUserEntity;
import com.stanzaliving.user.entity.OtpEntity;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface BlacklistUserDbService extends AbstractJpaService<BlacklistUserEntity, Long> {

	boolean checkIfMobileExist(String mobile);
	
	BlacklistUserEntity findByMobileNumber(String mobile);

}