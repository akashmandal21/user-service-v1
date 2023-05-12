package com.stanzaliving.user.utils;

import com.stanzaliving.core.base.exception.ManyDeviceLoginException;
import com.stanzaliving.core.user.enums.App;
import com.stanzaliving.user.entity.UserSessionEntity;
import com.stanzaliving.user.service.RedisOperationsService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Component
public class DeviceBlockUtil {

    @Autowired
    private RedisOperationsService redisOperationsService ;

    public final static String LockedEntitySet = "DEVICE_LOCKED_ENTITY_SET";
    @Value("${device.locktime.ALFRED}")
    public long lockTimeAlfred ;
    @Value("${device.locktime.SIGMA}")
    public long lockTimeSigma ;
    @Value("${device.whitelist.ALFRED}")
    private String whiteListDeviceAlfred;
    @Value("${device.whitelist.SIGMA}")
    private String whiteListDeviceSigma;
    @Value("${device.login.max.count.ALFRED}")
    public int maxDeviceAlfred ;
    @Value("${device.login.max.count.SIGMA}")
    public int maxDeviceSigma ;

    public void saveDeviceRedis(String mobileNumber, UserSessionEntity userSessionEntity){
        String key = userSessionEntity.getDevice()+"_"+userSessionEntity.getBrowser() ;
        log.info("Taking lock on user number {} and userSessionEntity : {} " , mobileNumber , userSessionEntity);
        Map<String, Long> lockTime = new HashMap<>();
        lockTime.put(App.ALFRED.name(), lockTimeAlfred);
        lockTime.put(App.SIGMA.name(), lockTimeSigma);
        Set<String> bypassDeviceAlfred = Stream.of(whiteListDeviceAlfred.trim().split("\\s*,\\s*")).collect(Collectors.toSet());
        Set<String> bypassDeviceSigma = Stream.of(whiteListDeviceSigma.trim().split("\\s*,\\s*")).collect(Collectors.toSet());
        if(!(bypassDeviceAlfred.contains(userSessionEntity.getDevice())||bypassDeviceSigma.contains(userSessionEntity.getDevice()) )){
            Map<String , UserSessionEntity> sessionEntityMap = (Map<String, UserSessionEntity>) redisOperationsService.getFromMap(LockedEntitySet,key);
            if(Objects.isNull(sessionEntityMap)){
                sessionEntityMap= new HashMap<>();
                sessionEntityMap.put(mobileNumber, userSessionEntity);
                redisOperationsService.putToMap(LockedEntitySet , key, sessionEntityMap ,lockTime.get(userSessionEntity.getBrowser()),TimeUnit.SECONDS);
            }else if (!sessionEntityMap.containsKey(mobileNumber)){
                    sessionEntityMap.put(mobileNumber,userSessionEntity);
                    redisOperationsService.putToMap(LockedEntitySet , key, sessionEntityMap ,lockTime.get(userSessionEntity.getBrowser()),TimeUnit.SECONDS);
            }
        }
        return ;
    }

    public void validateDevice(String deviceId , String appType , String  mobileNumber){
        String key = deviceId+"_"+appType ;
        log.info("checking device lock for  deviceId : {}, appName : {} user number {} " , deviceId , appType , mobileNumber);
        Set<String> bypassDeviceAlfred = Stream.of(whiteListDeviceAlfred.trim().split("\\s*,\\s*")).collect(Collectors.toSet());
        Set<String> bypassDeviceSigma = Stream.of(whiteListDeviceSigma.trim().split("\\s*,\\s*")).collect(Collectors.toSet());
        if(!(bypassDeviceAlfred.contains(deviceId)||bypassDeviceSigma.contains(deviceId) )){
            Map<String , UserSessionEntity> sessionEntityMap = (Map<String, UserSessionEntity>)  redisOperationsService.getFromMap(LockedEntitySet,key);
            if( MapUtils.isNotEmpty(sessionEntityMap) && !sessionEntityMap.containsKey(mobileNumber)){
                long finalLastLoginTime = 0l;
                long lastLoginTime = sessionEntityMap.entrySet().stream()
                        .filter(entry -> entry.getValue().getCreatedAt().getTime() > finalLastLoginTime)
                        .mapToLong(entry -> entry.getValue().getCreatedAt().getTime())
                        .max()
                        .orElse(0L);
                long blockTime = lastLoginTime+TimeUnit.SECONDS.toMillis(lockTimeAlfred) ;
                Map<String, Integer> maxDeviceLimits = new HashMap<>();
                maxDeviceLimits.put(App.ALFRED.name(), maxDeviceAlfred);
                maxDeviceLimits.put(App.SIGMA.name(), maxDeviceSigma);
                Integer maxDeviceLimit = maxDeviceLimits.get(appType);
                if (Objects.nonNull(maxDeviceLimit) && sessionEntityMap.size() >= maxDeviceLimit) {
                    throw new ManyDeviceLoginException("Hey, you've made multiple log-ins on this device from multiple numbers. Please try with only one phone number. Or try again after " + new Date(blockTime));
                }
            }
        }
        return  ;
    }
}
