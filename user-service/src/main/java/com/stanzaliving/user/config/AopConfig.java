package com.stanzaliving.user.config;

import com.stanzaliving.core.base.http.StanzaRestClient;
import com.stanzaliving.core.pushnotification.client.api.SlackNotification;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Aspect
@Configuration
@Log4j2
public class AopConfig {

    @Qualifier("slackClient")
    @Autowired
    private StanzaRestClient stanzaRestClient;

    private SlackNotification slackNotification;

    @Value("${enable.slack.exception}")
    private Boolean enableSlackException;

    @Value("${service.slack.exception.endUrl}")
    private String slackExceptionEndUrl;

    @PostConstruct
    public void init() {
        slackNotification = new SlackNotification(stanzaRestClient);
    }

    @Around(value = "execution(* com.stanzaliving.user.config.ExceptionInterceptor.*(..)) && @annotation(com.stanzaliving.core.pushnotification.client.api.annotation.SendExceptionToSlack)")
    public void sendToSlack(ProceedingJoinPoint proceedingJoinPoint) {
        if (enableSlackException) {
            log.debug("Sending exception to Slack through AOP");
            slackNotification.sendExceptionNotificationRequest((Exception) proceedingJoinPoint.getArgs()[0], slackExceptionEndUrl);
        }
    }
}
