package com.stanzaliving.user.utils;

import com.stanzaliving.core.base.exception.ManyDeviceLoginException;
import com.stanzaliving.user.entity.UserSessionEntity;
import com.stanzaliving.user.service.RedisOperationsService;
import lombok.extern.log4j.Log4j2;
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
    @Value("${device.locktime}")
    public long lockTime ;
    @Value("${device.whitelist}")
    private String whiteListDevice;
    @Value("${device.login.max.count.ALFRED}")
    public int maxDeviceAlfred ;
    @Value("${device.login.max.count.SIGMA}")
    public int maxDeviceSigma ;

    public void saveDeviceRedis(String mobileNumber, UserSessionEntity userSessionEntity){
        String key = userSessionEntity.getDevice()+"_"+userSessionEntity.getBrowser() ;
        log.info("Taking lock on user number {} and userSessionEntity : {} " , mobileNumber , userSessionEntity);
        Set<String> bypassDevice = Stream.of(whiteListDevice.trim().split("\\s*,\\s*")).collect(Collectors.toSet());
        if(!bypassDevice.contains(userSessionEntity.getDevice())){
            Map<String , UserSessionEntity> sessionEntityMap = (Map<String, UserSessionEntity>) redisOperationsService.getFromMap(LockedEntitySet,key);
            if(Objects.isNull(sessionEntityMap)){
                sessionEntityMap= new HashMap<>();
                sessionEntityMap.put(mobileNumber, userSessionEntity);
                redisOperationsService.putToMap(LockedEntitySet , key, sessionEntityMap ,lockTime,TimeUnit.SECONDS);
            }else if (!sessionEntityMap.containsKey(mobileNumber)){
                    sessionEntityMap.put(mobileNumber,userSessionEntity);
                    redisOperationsService.putToMap(LockedEntitySet , key, sessionEntityMap ,lockTime,TimeUnit.SECONDS);
            }
        }
        return ;
    }

    public void validateDevice(String deviceId , String appType , String  mobileNumber){
        String key = deviceId+"_"+appType ;
        log.info("checking device lock for  deviceId : {}, appName : {} user number {} " , deviceId , appType , mobileNumber);
        Set<String> bypassDevice = Stream.of(whiteListDevice.trim().split("\\s*,\\s*")).collect(Collectors.toSet());
        Map<String , UserSessionEntity> sessionEntityMap = (Map<String, UserSessionEntity>)  redisOperationsService.getFromMap(LockedEntitySet,key);
        if(!bypassDevice.contains(deviceId) && Objects.nonNull(sessionEntityMap) && !sessionEntityMap.containsKey(mobileNumber)){
            long finalLastLoginTime = 0l;
            long lastLoginTime = sessionEntityMap.entrySet().stream()
                    .filter(entry -> entry.getValue().getCreatedAt().getTime() > finalLastLoginTime)
                    .mapToLong(entry -> entry.getValue().getCreatedAt().getTime())
                    .max()
                    .orElse(0L);
            long currentTime = new Date().getTime();
            long diff = TimeUnit.MILLISECONDS.toSeconds(currentTime-lastLoginTime) ;
            long blockTime = lastLoginTime+TimeUnit.SECONDS.toMillis(lockTime) ;
            Map<String, Integer> maxDeviceLimits = new HashMap<>();
            maxDeviceLimits.put("ALFRED", maxDeviceAlfred);
            maxDeviceLimits.put("SIGMA", maxDeviceSigma);
            Integer maxDeviceLimit = maxDeviceLimits.get(appType);
            if (maxDeviceLimit != null && sessionEntityMap.size() >= maxDeviceLimit && (diff < lockTime)) {
                throw new ManyDeviceLoginException("Too Many logins on this device from different numbers. Please try with the same number, or try after " + new Date(blockTime));
            }
        }
        return  ;
    }
}
