package com.baiyi.opscloud.workorder.query.impl;

import com.baiyi.opscloud.domain.constants.DsAssetTypeConstants;
import com.baiyi.opscloud.domain.param.datasource.DsAssetParam;
import com.baiyi.opscloud.domain.param.workorder.WorkOrderTicketEntryParam;
import com.baiyi.opscloud.workorder.constants.WorkOrderKeyConstants;
import com.baiyi.opscloud.workorder.query.impl.extended.DatasourceAssetExtendedTicketEntryQuery;
import org.springframework.stereotype.Component;

/**
 * @Author baiyi
 * @Date 2022/1/11 6:20 PM
 * @Version 1.0
 */
@Component
public class RamPolicyEntryQuery extends DatasourceAssetExtendedTicketEntryQuery {

    @Override
    protected DsAssetParam.AssetPageQuery getAssetQueryParam(WorkOrderTicketEntryParam.EntryQuery entryQuery) {
       return DsAssetParam.AssetPageQuery.builder()
               .instanceUuid(entryQuery.getInstanceUuid())
               .assetType(DsAssetTypeConstants.RAM_POLICY.name())
               .queryName(entryQuery.getQueryName())
               .isActive(true)
               .build();
    }

    @Override
    public String getKey() {
        return WorkOrderKeyConstants.RAM_POLICY.name();
    }

}