package com.stanzaliving.user.service;

import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.user.acl.db.service.RoleDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelRoleDbService;
import com.stanzaliving.user.acl.entity.RoleEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import com.stanzaliving.user.db.service.UserDbService;
import com.stanzaliving.user.dto.userv2.*;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.entity.UserManagerMappingEntity;
import com.stanzaliving.user.entity.UserProfileEntity;
import com.stanzaliving.user.feignclient.MigrationHttpService;
import com.stanzaliving.user.repository.UserManagerMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MigrationService {

    @Autowired
    private MigrationHttpService migrationHttpService;

    @Autowired
    private UserDbService userDbService;

    @Autowired
    private UserManagerMappingRepository managerMappingRepository;

    @Autowired
    private RoleDbService roleDbService;

    @Autowired
    private UserDepartmentLevelDbService userDepartmentLevelDbService;

    @Autowired
    private UserDepartmentLevelRoleDbService userDepartmentLevelRoleDbService;

    public void migrateUsers() {
        List<UserEntity> userEntityList=userDbService.findByUserTypeIn(Arrays.asList(UserType.DESIGN_COORDINATOR,UserType.NATIONAL_HEAD,UserType.PROJECT_MANAGER,
                UserType.ZONAL_HEAD,UserType.SITE_ENGINEER));

        List<UserDto> userDtos=new ArrayList<>();
        List<UserEntity> userEntities=new ArrayList<>();
        for(UserEntity userEntity: userEntityList){
            UserProfileEntity userProfile=userEntity.getUserProfile();
            UserManagerMappingEntity userManagerMappingEntity=managerMappingRepository.findFirstByUserId(userEntity.getUuid());
            try {
                UserDto userDto=UserDto.builder()
                        .userUuid(userEntity.getUuid())
                        .birthday(userProfile.getBirthday())
                        .bloodGroup(userProfile.getBloodGroup())
                        .gender(userProfile.getGender())
                        .firstName(userProfile.getFirstName())
                        .lastName(userProfile.getLastName())
                        .middleName(userProfile.getMiddleName())
                        .emailId(userEntity.getEmail())
                        .mobileNumber(userEntity.getMobile())
                        .isoCode("IN")
                        .maritalStatus(userProfile.getMaritalStatus())
                        .userUuid(userEntity.getUuid())
                        .status(userEntity.isStatus())
                        .nationality(userProfile.getNationality())
                        .userProfileDto(UserProfileDto.builder()
                                .department(userEntity.getDepartment())
                                .userType(userEntity.getUserType())
                                .addressLine1("")
                                .addressLine2("")
                                .build())
                        .userAttributesDto(UserAttributesDto.
                                builder()
                                .managerUuid(Objects.nonNull(userManagerMappingEntity)?userManagerMappingEntity.getManagerId():null)
                                .userUuid(userEntity.getUuid())
                                .build())
                        .build();
                userDtos.add(userDto);
                userEntity.setUuid(userEntity.getUuid());
                userEntity.setStatus(false);
                userEntities.add(userEntity);
            }
            catch (Exception e){
                //throw new StanzaException("failed for useruuid "+userEntity.getUuid());
            }
        }
        migrationHttpService.migrateUsers(userDtos);
        userDbService.save(userEntities);
    }

    public void migrateRoles(){
        List<UserEntity> userEntityList=userDbService.findByUserTypeIn(Arrays.asList(UserType.DESIGN_COORDINATOR,UserType.NATIONAL_HEAD,UserType.PROJECT_MANAGER,
                UserType.ZONAL_HEAD,UserType.SITE_ENGINEER));
        if(Objects.nonNull(userEntityList)) {

            for(UserEntity userEntity:userEntityList){
                List<UserDepartmentLevelEntity> userDepartmentLevelEntities=userDepartmentLevelDbService.findByUserUuidAndStatus(userEntity.getUuid(),true);
                if(Objects.nonNull(userDepartmentLevelEntities)) {
                    for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntities) {
                        RoleDto roleDto = RoleDto.builder()
                                .accessLevel(userDepartmentLevelEntity.getAccessLevel())
                                .department(userDepartmentLevelEntity.getDepartment())
                                .roleName(userEntity.getUserType().toString())
                                .build();
                        migrationHttpService.migrateRoles(roleDto);
                    }
                }
            }
//
//
//            List<String> userUuids = userEntityList.stream().map(f -> f.getUuid()).collect(Collectors.toList());
//            List<UserDepartmentLevelEntity> userDepartmentLevelEntities=userDepartmentLevelDbService.findByUserUuidIn(userUuids);
//            if(Objects.nonNull(userDepartmentLevelEntities)){
//                List<String> udUuids=userDepartmentLevelEntities.stream().map(f->f.getUuid()).collect(Collectors.toList());
//                List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntities=userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuidIn(udUuids);
//                if(Objects.nonNull(userDepartmentLevelRoleEntities)){
//                    List<String> roleUuids=userDepartmentLevelRoleEntities.stream().map(f->f.getRoleUuid()).collect(Collectors.toList());
//                    List<RoleEntity> roleEntities=roleDbService.findByUuidIn(roleUuids);
//                    migrationHttpService.migrateRoles(roleEntities);
//                }
//            }
        }
    }

    public void migrateUserRoleMapping(){
        List<UserEntity> userEntityList=userDbService.findByUserTypeIn(Arrays.asList(UserType.DESIGN_COORDINATOR,UserType.NATIONAL_HEAD,UserType.PROJECT_MANAGER,
                UserType.ZONAL_HEAD,UserType.SITE_ENGINEER));
        if(Objects.nonNull(userEntityList)) {

            for(UserEntity userEntity: userEntityList){
                List<UserDepartmentLevelEntity> userDepartmentLevelEntities=userDepartmentLevelDbService.findByUserUuidAndStatus(userEntity.getUuid(),true);
                if (Objects.nonNull(userDepartmentLevelEntities)) {
                    for(UserDepartmentLevelEntity userDepartmentLevelEntity: userDepartmentLevelEntities){
                        List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntities=userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuid(userDepartmentLevelEntity.getUuid());
                        if(Objects.nonNull(userDepartmentLevelRoleEntities)){
                            for(UserDepartmentLevelRoleEntity userDepartmentLevelRoleEntity: userDepartmentLevelRoleEntities){
                                RoleEntity roleEntity=roleDbService.findByUuid(userDepartmentLevelRoleEntity.getRoleUuid());
                                if(Objects.nonNull(roleEntity)) {

                                    RoleDto roleDto = RoleDto.builder()
                                            .accessLevel(userDepartmentLevelEntity.getAccessLevel())
                                            .department(userDepartmentLevelEntity.getDepartment())
                                            .roleName(userEntity.getUserType().toString())
                                            .build();

                                    UserRoleMappingMigrationDto userRoleMappingMigrationDto=UserRoleMappingMigrationDto.builder()
                                            .accesslevelUuids(userDepartmentLevelEntity.getCsvAccessLevelEntityUuid())
                                            .roleUuid(roleEntity.getUuid())
                                            .accessLevel(userDepartmentLevelEntity.getAccessLevel())
                                            .userUuid(userEntity.getUuid())
                                            .department(userDepartmentLevelEntity.getDepartment())
                                            .build();

                                    UserMigrationRoleAndAssignmentDto userMigrationRoleAndAssignmentDto=UserMigrationRoleAndAssignmentDto.builder()
                                            .roleDto(roleDto)
                                            .userRoleMappingMigrationDto(userRoleMappingMigrationDto)
                                            .build();
                                    migrationHttpService.migrateUserRoleMapping(userMigrationRoleAndAssignmentDto);
//                                    roleEntity.setStatus(false);
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}
