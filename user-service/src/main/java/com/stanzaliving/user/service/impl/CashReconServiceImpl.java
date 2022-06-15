package com.stanzaliving.user.service.impl;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.venta_aggregation_client.api.VentaAggregationServiceApi;
import com.stanzaliving.core.ventaaggregationservice.dto.BookingResidenceAggregationEntityDto;
import com.stanzaliving.core.ventaaggregationservice.dto.ResidenceFilterRequestDto;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelDbService;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.service.AclUserService;
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

import java.util.*;

@Log4j2
@Service
public class CashReconServiceImpl implements CashReconService {

    @Autowired
    private UserDepartmentLevelDbService userDepartmentLevelDbService;

    @Autowired
    private VentaAggregationServiceApi ventaAggregationServiceApi;

    @Autowired
    private AclUserService aclUserService;

    @Autowired
    private UserDbService userDbService;

    @Override
    public List<CashReconReceiverInfo> getReceiverList(CashReconReceiverRequest cashReconReceiverRequest) {
        List<CashReconReceiverInfo> cashReceiverInfoList = new ArrayList<>();
        String userUuidOfDepositor = cashReconReceiverRequest.getUserUuid();
        TransferTo transferTo = cashReconReceiverRequest.getTransferTo();

        List<UserDepartmentLevelEntity> userDepartmentLevelEntityList =
                userDepartmentLevelDbService.findByUserUuidAndDepartmentAndStatus(userUuidOfDepositor, Department.SALES, true);

        if (CollectionUtils.isEmpty(userDepartmentLevelEntityList))
            return cashReceiverInfoList;
        Optional<UserDepartmentLevelEntity> userDepartmentLevelEntity;
        List<String> residenceIds;
        List<String> microMarketIds = new ArrayList<>();
        ;
        List<String> cityIds = new ArrayList<>();
        String cityId;
        String microMarketId;
        if (TransferTo.CLUSTER_MANAGER.equals(transferTo)) {
            userDepartmentLevelEntity =
                    userDepartmentLevelEntityList.stream().filter(x -> x.getAccessLevel().equals(AccessLevel.RESIDENCE))
                            .findFirst();
            if (userDepartmentLevelEntity.isPresent()) {
                residenceIds = Arrays.asList(userDepartmentLevelEntity.get().getCsvAccessLevelEntityUuid().split(","));
                for (String residenceId : residenceIds) {
                    microMarketId = ventaAggregationServiceApi.getAggregatedResidenceInformation(residenceId).getData().getMicroMarketId();
                    if (!microMarketIds.contains(microMarketId))
                        microMarketIds.add(microMarketId);
                }
                return getClusterManagerOrNodalList(microMarketIds, transferTo);
            } else {
                userDepartmentLevelEntity =
                        userDepartmentLevelEntityList.stream().filter(x -> x.getAccessLevel().equals(AccessLevel.MICROMARKET))
                                .findFirst();
                if (userDepartmentLevelEntity.isPresent()) {
                    microMarketIds = Arrays.asList(userDepartmentLevelEntity.get().getCsvAccessLevelEntityUuid().split(","));
                    return getClusterManagerOrNodalList(microMarketIds, transferTo);
                }
            }

        } else if (TransferTo.NODAL.equals(transferTo)) {
            userDepartmentLevelEntity =
                    userDepartmentLevelEntityList.stream().filter(x -> x.getAccessLevel().equals(AccessLevel.RESIDENCE))
                            .findFirst();
            if (userDepartmentLevelEntity.isPresent()) {
                residenceIds = Arrays.asList(userDepartmentLevelEntity.get().getCsvAccessLevelEntityUuid().split(","));
                for (String residenceId : residenceIds) {
                    cityId = ventaAggregationServiceApi.getAggregatedResidenceInformation(residenceId).getData().getCityId() + "";
                    cityIds.add(cityId);
                }
                return getClusterManagerOrNodalList(cityIds, transferTo);
            } else {
                userDepartmentLevelEntity =
                        userDepartmentLevelEntityList.stream().filter(x -> x.getAccessLevel().equals(AccessLevel.MICROMARKET))
                                .findFirst();
                if (userDepartmentLevelEntity.isPresent()) {
                    microMarketIds = Arrays.asList(userDepartmentLevelEntity.get().getCsvAccessLevelEntityUuid().split(","));
                    for (String microMarketid : microMarketIds) {
                        List<BookingResidenceAggregationEntityDto> bookingResidenceAggregationEntityDtoList = ventaAggregationServiceApi.getResidenceListing(ResidenceFilterRequestDto.builder().
                                microMarketIdList(Collections.singleton(microMarketid)).build()).getData().getContent();
                        for(BookingResidenceAggregationEntityDto bookingResidenceAggregationEntityDto : bookingResidenceAggregationEntityDtoList){
                            cityIds.add(bookingResidenceAggregationEntityDto.getCityId()+"");
                        }
                    }
                    return getClusterManagerOrNodalList(cityIds, transferTo);
                } else {
                    userDepartmentLevelEntity =
                            userDepartmentLevelEntityList.stream().filter(x -> x.getAccessLevel().equals(AccessLevel.CITY))
                                    .findFirst();
                    if (userDepartmentLevelEntity.isPresent()) {
                        cityIds = Arrays.asList(userDepartmentLevelEntity.get().getCsvAccessLevelEntityUuid().split(","));
                        return getClusterManagerOrNodalList(cityIds, transferTo);
                    }
                }
            }
        }

        return cashReceiverInfoList;
    }

    @Override
    public List<NodalOfficerInfo> getNodalOfficersList() {
        return null;
    }

    private List<CashReconReceiverInfo> getClusterManagerOrNodalList(List<String> ids, TransferTo transferTo) {
        List<CashReconReceiverInfo> cashReconReceiverInfoList = new ArrayList<>();
        Map<String, List<String>> userIdMapping = new HashMap<>();
        if (TransferTo.CLUSTER_MANAGER.equals(transferTo)) {
            userIdMapping = aclUserService.getActiveUsersForRoles(Department.OPS, "CM_CASH_LEDGER_EDITOR", ids);
        } else if (TransferTo.NODAL.equals(transferTo)) {
            userIdMapping = aclUserService.getActiveUsersForRoles(Department.OPS, "NODAL_CASH_LEDGER_EDITOR", ids);
        }
        if(Objects.nonNull(userIdMapping) && userIdMapping.size() > 0){
            buildCashReceiverInfo(cashReconReceiverInfoList, userIdMapping);
        }
        return cashReconReceiverInfoList;
    }

    private void buildCashReceiverInfo(List<CashReconReceiverInfo> cashReconReceiverInfoList, Map<String, List<String>> userIdMapping) {
        if (Objects.nonNull(userIdMapping) && userIdMapping.size() > 0) {
            Set<String> userIds = userIdMapping.keySet();
            for (String userId : userIds) {
                UserEntity userEntity = userDbService.findByUuidAndStatus(userId, true);
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
