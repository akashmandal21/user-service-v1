package com.stanzaliving.user.service.impl;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.user.db.service.BlacklistUserDbService;
import com.stanzaliving.user.entity.BlacklistUserEntity;
import com.stanzaliving.user.service.BlacklistUserService;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class BlacklistUserServiceImpl implements BlacklistUserService {

	@Autowired
	private BlacklistUserDbService blacklistUserDbService;
	
	@Override
	public boolean checkIfUserIsBlacklisted(String mobile) {
		
		log.info("Got request to check blacklist {}",mobile);
		
		return blacklistUserDbService.checkIfMobileExist(mobile);
	}

	@Override
	public boolean addToBlacklist(String mobile) {
		
		log.info("Got request to add to blacklist {}",mobile);
		
		BlacklistUserEntity blacklistUserEntity = blacklistUserDbService.findByMobileNumber(mobile);
		
		if(Objects.nonNull(blacklistUserEntity)) {
			blacklistUserEntity.setStatus(true);
		}else {
			blacklistUserEntity = BlacklistUserEntity.builder().mobile(mobile).status(true).build();
		}
		
		blacklistUserEntity = blacklistUserDbService.save(blacklistUserEntity);
		
		return Objects.nonNull(blacklistUserEntity);
	}

	@Override
	public boolean removeFromBlacklist(String mobile) {

		log.info("Got request to remove from blacklist {}",mobile);

		BlacklistUserEntity blacklistUserEntity = blacklistUserDbService.findByMobileNumber(mobile);
		
		if(Objects.nonNull(blacklistUserEntity)) {
			blacklistUserEntity.setStatus(false);
		}else {
			throw new StanzaException("User not found");
		}
		
		blacklistUserEntity = blacklistUserDbService.save(blacklistUserEntity);
		
		return Objects.nonNull(blacklistUserEntity);
	}

}
