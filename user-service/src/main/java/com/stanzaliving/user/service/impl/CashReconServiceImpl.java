package com.stanzaliving.user.service.impl;

import com.stanzaliving.core.base.common.dto.AbstractDto;
import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.transformation.client.api.InternalDataControllerApi;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.transformations.pojo.MicroMarketMetadataDto;
import com.stanzaliving.transformations.pojo.ResidenceDto;
import com.stanzaliving.transformations.pojo.ResidenceUIDto;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelRoleDbService;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import com.stanzaliving.user.acl.service.AclUserService;
import com.stanzaliving.user.acl.service.RoleService;
import com.stanzaliving.user.db.service.UserDbService;
import com.stanzaliving.user.dto.request.CashReconReceiverRequest;
import com.stanzaliving.user.dto.response.CashReconReceiverInfo;
import com.stanzaliving.user.dto.response.NodalOfficerInfo;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.enums.TransferTo;
import com.stanzaliving.user.service.CashReconService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
public class CashReconServiceImpl implements CashReconService {

    @Autowired
    private UserDepartmentLevelDbService userDepartmentLevelDbService;

    @Autowired
    private AclUserService aclUserService;

    @Autowired
    private UserDbService userDbService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserDepartmentLevelRoleDbService userDepartmentLevelRoleDbService;

    @Autowired
    private InternalDataControllerApi transformationClientApi;

    @Override
    public List<CashReconReceiverInfo> getReceiverList(CashReconReceiverRequest cashReconReceiverRequest) {
        List<CashReconReceiverInfo> cashReceiverInfoList = new ArrayList<>();
        String userUuidOfDepositor = cashReconReceiverRequest.getUserUuid();
        TransferTo transferTo = cashReconReceiverRequest.getTransferTo();

        List<UserDepartmentLevelEntity> userDepartmentLevelEntityList =
                userDepartmentLevelDbService.findByUserUuidAndDepartmentAndStatus(userUuidOfDepositor, Department.OPS, true);

        if (CollectionUtils.isEmpty(userDepartmentLevelEntityList))
            return cashReceiverInfoList;
        log.info("userDepartmentLevelEntityList is {}", userDepartmentLevelEntityList);
        Optional<UserDepartmentLevelEntity> userDepartmentLevelEntity;
        List<String> residenceIds;
        List<String> microMarketIds = new ArrayList<>();
        List<String> cityIds = new ArrayList<>();
        String cityId;
        String microMarketId;
        if (TransferTo.CLUSTER_MANAGER.equals(transferTo)) {
            userDepartmentLevelEntity =
                    userDepartmentLevelEntityList.stream().filter(x -> AccessLevel.RESIDENCE.equals(x.getAccessLevel()))
                            .findFirst();
            if (userDepartmentLevelEntity.isPresent()) {
                residenceIds = Arrays.asList(userDepartmentLevelEntity.get().getCsvAccessLevelEntityUuid().split(","));
                for (String residenceId : residenceIds) {
                    ResponseDto<ResidenceUIDto> residenceAggregationEntityDtoResponseDto = transformationClientApi.getResidenceDetail(residenceId);
                    if (Objects.nonNull(residenceAggregationEntityDtoResponseDto)) {
                        ResidenceUIDto residenceFilterRequestDto = residenceAggregationEntityDtoResponseDto.getData();
                        if (Objects.nonNull(residenceFilterRequestDto)) {
                            microMarketId = residenceFilterRequestDto.getMicroMarketId() + "";
                            if (!StringUtils.isEmpty(microMarketId) && !microMarketIds.contains(microMarketId))
                                microMarketIds.add(microMarketId);
                        }
                    }
                }
                return getClusterManagerOrNodalList(microMarketIds, transferTo, userUuidOfDepositor);
            } else {
                userDepartmentLevelEntity =
                        userDepartmentLevelEntityList.stream().filter(x -> AccessLevel.MICROMARKET.equals(x.getAccessLevel()))
                                .findFirst();
                if (userDepartmentLevelEntity.isPresent()) {
                    microMarketIds = Arrays.asList(userDepartmentLevelEntity.get().getCsvAccessLevelEntityUuid().split(","));
                    return getClusterManagerOrNodalList(microMarketIds, transferTo, userUuidOfDepositor);
                }
            }

        } else if (TransferTo.NODAL_OFFICER.equals(transferTo)) {
            userDepartmentLevelEntity =
                    userDepartmentLevelEntityList.stream().filter(x -> AccessLevel.RESIDENCE.equals(x.getAccessLevel()))
                            .findFirst();
            if (userDepartmentLevelEntity.isPresent()) {
                residenceIds = Arrays.asList(userDepartmentLevelEntity.get().getCsvAccessLevelEntityUuid().split(","));
                for (String residenceId : residenceIds) {
                    ResponseDto<ResidenceUIDto> aggregationEntityResponseDto = transformationClientApi.getResidenceDetail(residenceId);
                    if (Objects.nonNull(aggregationEntityResponseDto)) {
                        ResidenceUIDto aggregationEntityDto = aggregationEntityResponseDto.getData();
                        cityId = Objects.nonNull(aggregationEntityDto) ? aggregationEntityDto.getCityId() + "" : null;
                        if (!StringUtils.isEmpty(cityId))
                            cityIds.add(cityId);
                    }
                }
                return getClusterManagerOrNodalList(cityIds, transferTo, userUuidOfDepositor);
            } else {
                userDepartmentLevelEntity =
                        userDepartmentLevelEntityList.stream().filter(x -> AccessLevel.MICROMARKET.equals(x.getAccessLevel()))
                                .findFirst();
                if (userDepartmentLevelEntity.isPresent()) {
                    microMarketIds = Arrays.asList(userDepartmentLevelEntity.get().getCsvAccessLevelEntityUuid().split(","));
                    for (String microMarketid : microMarketIds) {
                        ResponseDto<List<ResidenceDto>>  bookingResidenceAggregationEntityDtoResponse = transformationClientApi.getResidencesByMicromarketUuid(microMarketid);
                        if(Objects.nonNull(bookingResidenceAggregationEntityDtoResponse) && Objects.nonNull(bookingResidenceAggregationEntityDtoResponse.getData())) {
                            List<ResidenceDto> bookingResidenceAggregationEntityDtoList = bookingResidenceAggregationEntityDtoResponse.getData();
                            for (ResidenceDto bookingResidenceAggregationEntityDto : bookingResidenceAggregationEntityDtoList) {
                                if (Objects.nonNull(bookingResidenceAggregationEntityDto)) {
                                    ResponseDto<MicroMarketMetadataDto> microMarketMetadataResponseDto =
                                            transformationClientApi.getMicromarketData(bookingResidenceAggregationEntityDto.getMicromarketUuid());
                                    if(Objects.nonNull(microMarketMetadataResponseDto) && Objects.nonNull(microMarketMetadataResponseDto.getData())) {
                                        String cityUuid = microMarketMetadataResponseDto.getData().getCityId() + "";
                                        if(!StringUtils.isEmpty(cityUuid))
                                            cityIds.add(cityUuid);
                                    }
                                }
                            }
                        }
                    }
                    return getClusterManagerOrNodalList(cityIds, transferTo, userUuidOfDepositor);
                } else {
                    userDepartmentLevelEntity =
                            userDepartmentLevelEntityList.stream().filter(x -> AccessLevel.CITY.equals(x.getAccessLevel()))
                                    .findFirst();
                    if (userDepartmentLevelEntity.isPresent()) {
                        cityIds = Arrays.asList(userDepartmentLevelEntity.get().getCsvAccessLevelEntityUuid().split(","));
                        return getClusterManagerOrNodalList(cityIds, transferTo, userUuidOfDepositor);
                    }
                }
            }
        }

        return cashReceiverInfoList;
    }

    @Override
    public List<NodalOfficerInfo> getNodalOfficersList() {
        List<RoleDto> roleDtoList =
                roleService.findByRoleNameInAndDepartment(Arrays.asList("NODAL_CASH_LEDGER_VIEWER", "NODAL_CASH_LEDGER_EDITOR"), Department.OPS);
        List<NodalOfficerInfo> nodalOfficerInfoList = new ArrayList<>();
        log.info("RoleDtoList is : {}", roleDtoList);
        List<String> roleUuids = roleDtoList.stream().map(AbstractDto::getUuid).collect(Collectors.toList());
        log.info("RoleUuids are : {}", roleUuids);
        List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = userDepartmentLevelRoleDbService.findByRoleUuidInAndStatus(roleUuids, true);
        log.info("userDepartmentLevelRoleEntityList is : {}", userDepartmentLevelRoleEntityList);
        List<String> userDepartmentLevelIds = userDepartmentLevelRoleEntityList.stream().map(UserDepartmentLevelRoleEntity::getUserDepartmentLevelUuid).collect(Collectors.toList());
        log.info("userDepartmentLevelIds are : {}", userDepartmentLevelIds);
        List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUuidInAndStatus(userDepartmentLevelIds, true);
        log.info("userDepartmentLevelEntityList is {}", userDepartmentLevelEntityList);
        List<String> userUuids = userDepartmentLevelEntityList.stream().map(UserDepartmentLevelEntity::getUserUuid).collect(Collectors.toList());
        log.info("userUuids are : {}", userUuids);
        for(String userId : userUuids){
            UserEntity userEntity = userDbService.findByUuidAndStatus(userId, true);
            nodalOfficerInfoList.add(NodalOfficerInfo.builder()
                    .uuid(userEntity.getUuid())
                    .name(userEntity.getUserProfile().getFirstName() + " " + userEntity.getUserProfile().getLastName())
                    .build());
        }
        log.info("nodalOfficerInfoList is : {}", nodalOfficerInfoList);
        return nodalOfficerInfoList;
    }

    private List<CashReconReceiverInfo> getClusterManagerOrNodalList(List<String> ids, TransferTo transferTo, String userUuidOfDepositor) {
        List<CashReconReceiverInfo> cashReconReceiverInfoList = new ArrayList<>();
        Map<String, List<String>> userIdMapping = new HashMap<>();
        if (TransferTo.CLUSTER_MANAGER.equals(transferTo)) {
            userIdMapping = aclUserService.getActiveUsersForRoles(Department.OPS, "CM_CASH_LEDGER_EDITOR", ids);
        } else if (TransferTo.NODAL_OFFICER.equals(transferTo)) {
            userIdMapping = aclUserService.getActiveUsersForRoles(Department.OPS, "NODAL_CASH_LEDGER_EDITOR", ids);
        }
        log.info("cashReconReceiverInfoList is {}", cashReconReceiverInfoList);
        if (CollectionUtils.isEmpty(userIdMapping)) return cashReconReceiverInfoList;

        userIdMapping.remove(userUuidOfDepositor);
        buildCashReceiverInfo(cashReconReceiverInfoList, userIdMapping);

        return cashReconReceiverInfoList;
    }

    private void buildCashReceiverInfo(List<CashReconReceiverInfo> cashReconReceiverInfoList, Map<String, List<String>> userIdMapping) {
        if (CollectionUtils.isEmpty(userIdMapping)) return;
        Set<String> userIds = userIdMapping.keySet();
        for (String userId : userIds) {
            UserEntity userEntity = userDbService.findByUuidAndStatus(userId, true);
            if (Objects.nonNull(userEntity)) {
                cashReconReceiverInfoList.add(CashReconReceiverInfo.builder()
                        .userUuid(userId)
                        .name(userEntity.getUserProfile().getFirstName() + " " + userEntity.getUserProfile().getLastName())
                        .phone(userEntity.getMobile())
                        .email(userEntity.getEmail())
                        .build());
            }
        }
    }
}
