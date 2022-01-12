package com.baiyi.opscloud.workorder.query.impl;

import com.baiyi.opscloud.common.util.JSONUtil;
import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.constants.BusinessTypeEnum;
import com.baiyi.opscloud.domain.generator.opscloud.ServerGroup;
import com.baiyi.opscloud.domain.param.server.ServerGroupParam;
import com.baiyi.opscloud.domain.param.workorder.WorkOrderTicketEntryParam;
import com.baiyi.opscloud.domain.vo.workorder.WorkOrderTicketVO;
import com.baiyi.opscloud.service.server.ServerGroupService;
import com.baiyi.opscloud.workorder.constants.WorkOrderKeyConstants;
import com.baiyi.opscloud.workorder.query.impl.base.BaseTicketEntryQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author baiyi
 * @Date 2022/1/11 4:40 PM
 * @Version 1.0
 */
@Component
public class ApplicationEntryQuery extends BaseTicketEntryQuery<ServerGroup> {

    @Resource
    private ServerGroupService serverGroupService;

    @Override
    protected List<ServerGroup> queryEntries(WorkOrderTicketEntryParam.EntryQuery entryQuery){
        ServerGroupParam.ServerGroupPageQuery pageQuery = ServerGroupParam.ServerGroupPageQuery.builder()
                .name(entryQuery.getQueryName())
                .extend(false)
                .page(1)
                .length(entryQuery.getLength())
                .build();
        DataTable<ServerGroup> dataTable = serverGroupService.queryPageByParam(pageQuery);
        return dataTable.getData();
    }

    @Override
    protected WorkOrderTicketVO.Entry toEntry(WorkOrderTicketEntryParam.EntryQuery entryQuery, ServerGroup entry) {
        return WorkOrderTicketVO.Entry.builder()
                .workOrderTicketId(entryQuery.getWorkOrderTicketId())
                .name(entry.getName())
                .entryKey(entry.getName())
                .businessType(BusinessTypeEnum.SERVERGROUP.getType())
                .businessId(entry.getId())
                .content(JSONUtil.writeValueAsString(entry))
                .entry(entry)
                .build();
    }

    @Override
    public String getKey() {
        return WorkOrderKeyConstants.SERVER_GROUP.name();
    }

}