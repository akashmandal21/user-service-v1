package com.stanzaliving.user.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.stanzaliving.user.service.UserService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class SendNotificationToUsersWithBirthday {
	
	@Value("${jobs.enabled}")
	private boolean jobsEnabled;
	
	@Autowired
	private UserService userService;
	
	
	 @Scheduled(cron = "0 23 13 * * *")
	   public void cronJobSch() {

		 log.info("Send notification to users who have birthday today:: Starting send notifications to users having birthday today Job");
			
			if(jobsEnabled) {
				
				List<String> userIdList = userService.getUserProfileDtoWhoseBirthdayIsToday();
				
				if(userIdList.size() > 0) {
					
					log.info("List is: {}", userIdList.toString());
					
				}
				
				else {
					
					log.info("No elements found in list.");
					
				}
				
			}
			else {
				log.info("SendNotificationToUsersWhoHaveBirthdayTodayJob:: Jobs is disabled");
			}
			
			log.info("SendNotificationToUsersWhoHaveBirthdayTodayJob:: Finished User Sending Notification to users who have birthday today Job.");
	   }
	
	
	
	
	

}
