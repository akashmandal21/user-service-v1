/**
 * 
 */
package com.stanzaliving.user.db.service;

import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.core.user.enums.OtpType;
import com.stanzaliving.user.entity.PauseOtpEntity;
import com.stanzaliving.user.entity.OtpEntity;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface PauseOtpDbService extends AbstractJpaService<PauseOtpEntity, Long> {

	boolean checkIfMobileExist(String mobile);
	
	PauseOtpEntity findByMobileNumber(String mobile);

}