package com.baiyi.opscloud.service.leo;

import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.generator.opscloud.LeoBuild;
import com.baiyi.opscloud.domain.param.leo.LeoBuildParam;
import com.baiyi.opscloud.domain.param.leo.LeoJobParam;
import com.baiyi.opscloud.domain.param.leo.request.SubscribeLeoBuildRequestParam;
import com.baiyi.opscloud.domain.vo.base.ReportVO;

import java.util.List;

/**
 * @Author baiyi
 * @Date 2022/11/8 16:03
 * @Version 1.0
 */
public interface LeoBuildService {

    void add(LeoBuild leoBuild);

    LeoBuild getById(Integer id);

    void updateByPrimaryKeySelective(LeoBuild leoBuild);

    /**
     * 查询job的最大构建编号
     *
     * @param jobId
     * @return
     */
    int getMaxBuildNumberWithJobId(Integer jobId);

    List<LeoBuild> queryTheHistoricalBuildToBeDeleted(Integer jobId);

    DataTable<LeoBuild> queryBuildPage(SubscribeLeoBuildRequestParam pageQuery);

    DataTable<LeoBuild> queryBuildPage(LeoJobParam.JobBuildPageQuery pageQuery);

    List<LeoBuild> queryBuildVersion(LeoBuildParam.QueryDeployVersion queryBuildVersion);

    int countWithJobId(Integer jobId);

    List<LeoBuild> queryLatestBuildWithJobId(Integer jobId, int size);

    List<LeoBuild> queryUnfinishBuildWithOcInstance(String ocInstance);

    int countRunningWithJobId(int jobId);

    List<ReportVO.Report> statByMonth();

    List<ReportVO.Report> queryMonth();

    int countWithReport();

}
