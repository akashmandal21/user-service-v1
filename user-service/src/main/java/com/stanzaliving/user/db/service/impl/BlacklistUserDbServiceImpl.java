package com.stanzaliving.user.db.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.sqljpa.service.impl.AbstractJpaServiceImpl;
import com.stanzaliving.user.db.service.BlacklistUserDbService;
import com.stanzaliving.user.entity.BlacklistUserEntity;
import com.stanzaliving.user.repository.BlacklistUserRepository;

@Service
public class BlacklistUserDbServiceImpl extends AbstractJpaServiceImpl<BlacklistUserEntity, Long, BlacklistUserRepository>  implements BlacklistUserDbService {

	@Autowired
	private BlacklistUserRepository blacklistUserRepository;
	
	@Override
	public boolean checkIfMobileExist(String mobile) {
		return getJpaRepository().existsByMobileAndStatus(mobile, true);
	}

	@Override
	public BlacklistUserEntity findByMobileNumber(String mobile) {
		return getJpaRepository().findByMobile(mobile);
	}

	@Override
	protected BlacklistUserRepository getJpaRepository() {
		return blacklistUserRepository;
	}


}
