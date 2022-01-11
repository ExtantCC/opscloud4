package com.baiyi.opscloud.service.workorder.impl;

import com.baiyi.opscloud.domain.generator.opscloud.WorkOrderTicketEntry;
import com.baiyi.opscloud.mapper.opscloud.WorkOrderTicketEntryMapper;
import com.baiyi.opscloud.service.workorder.WorkOrderTicketEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Author baiyi
 * @Date 2022/1/6 7:43 PM
 * @Version 1.0
 */
@Service
@RequiredArgsConstructor
public class WorkOrderTicketEntryServiceImpl implements WorkOrderTicketEntryService {

    private final WorkOrderTicketEntryMapper workOrderTicketEntryMapper;

    @Override
    public WorkOrderTicketEntry getById(Integer id) {
        return workOrderTicketEntryMapper.selectByPrimaryKey(id);
    }

    @Override
    public void deleteById(Integer id) {
        workOrderTicketEntryMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void add(WorkOrderTicketEntry workOrderTicketEntry) {
        workOrderTicketEntryMapper.insert(workOrderTicketEntry);
    }

    @Override
    public void update(WorkOrderTicketEntry workOrderTicketEntry) {
        workOrderTicketEntryMapper.updateByPrimaryKey(workOrderTicketEntry);
    }

    @Override
    public List<WorkOrderTicketEntry> queryByWorkOrderTicketId(Integer workOrderTicketId) {
        Example example = new Example(WorkOrderTicketEntry.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("workOrderTicketId", workOrderTicketId);
        return workOrderTicketEntryMapper.selectByExample(example);
    }

}
