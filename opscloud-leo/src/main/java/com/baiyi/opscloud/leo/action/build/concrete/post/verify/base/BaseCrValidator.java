package com.baiyi.opscloud.leo.action.build.concrete.post.verify.base;

import com.baiyi.opscloud.common.datasource.base.BaseDsConfig;
import com.baiyi.opscloud.core.InstanceHelper;
import com.baiyi.opscloud.core.factory.DsConfigHelper;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceConfig;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstance;
import com.baiyi.opscloud.domain.generator.opscloud.LeoBuild;
import com.baiyi.opscloud.domain.generator.opscloud.LeoJob;
import com.baiyi.opscloud.leo.action.build.concrete.post.verify.factory.CrValidatorFactory;
import com.baiyi.opscloud.leo.constants.BuildDictConstants;
import com.baiyi.opscloud.leo.domain.model.LeoBuildModel;
import com.baiyi.opscloud.leo.domain.model.LeoJobModel;
import com.baiyi.opscloud.leo.exception.LeoBuildException;
import com.baiyi.opscloud.leo.helper.BuildingLogHelper;
import com.baiyi.opscloud.service.sys.EnvService;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;

/**
 * @Author baiyi
 * @Date 2022/11/18 15:07
 * @Version 1.0
 */
public abstract class BaseCrValidator<T extends BaseDsConfig> implements InitializingBean {

    public interface CrTypes {
        String ACR = "ACR";
        String ECR = "ECR";
    }

    /**
     * 查询镜像条数(任务并发会创建多个镜像)
     */
    protected static final int QUERY_IMAGES_SIZE = 5;

    @Resource
    protected EnvService envService;

    @Resource
    protected DsConfigHelper dsConfigHelper;

    @Resource
    private InstanceHelper instanceHelper;

    @Resource
    protected BuildingLogHelper logHelper;

    /**
     * 预检查
     *
     * @param leoJob
     * @param cr
     */
    protected void preInspection(LeoJob leoJob, LeoJobModel.CR cr, LeoBuildModel.BuildConfig buildConfig) {
        preInspection(leoJob, cr);
        Map<String, String> dict = Optional.ofNullable(buildConfig)
                .map(LeoBuildModel.BuildConfig::getBuild)
                .map(LeoBuildModel.Build::getDict)
                .orElseThrow(() -> new LeoBuildException("无法获取构建字典"));
        if (!dict.containsKey(BuildDictConstants.IMAGE_TAG.getKey())) {
            throw new LeoBuildException("无法从构建字典中获取镜像标签: dict.imageTag");
        }
    }

    /**
     * 预检查
     *
     * @param leoJob
     * @param cr
     */

    protected void preInspection(LeoJob leoJob, LeoJobModel.CR cr) {
        LeoJobModel.CRInstance crInstance = Optional.of(cr)
                .map(LeoJobModel.CR::getInstance)
                .orElseThrow(() -> new LeoBuildException("任务配置不存在无法验证镜像是否推送成功: job.cr.instance"));
        Optional.of(crInstance)
                .map(LeoJobModel.CRInstance::getRegionId)
                .orElseThrow(() -> new LeoBuildException("任务配置不存在无法验证镜像是否推送成功: job.cr.instance.regionId"));
        Optional.of(cr)
                .map(LeoJobModel.CR::getRepo)
                .map(LeoJobModel.Repo::getName)
                .orElseThrow(() -> new LeoBuildException("任务配置不存在无法验证镜像是否推送成功: job.cr.repo.name"));
    }

    public void verifyImage(LeoJob leoJob, LeoBuild leoBuild, LeoJobModel.CR cr, LeoBuildModel.BuildConfig buildConfig) {
        // 预检查
        preInspection(leoJob, cr, buildConfig);
        T dsConfig = getDsConfigByUuid(getDsInstanceUuid(cr));
        handleVerifyImage(leoJob, cr, buildConfig, dsConfig);
        logHelper.info(leoBuild, "校验CR镜像成功");
    }

    public void createRepository(LeoJob leoJob, LeoJobModel.CR cr) {
        // 预检查
        preInspection(leoJob, cr);
        T dsConfig = getDsConfigByUuid(getDsInstanceUuid(cr));
        handleCreateRepository(leoJob, cr, dsConfig);
    }

    /**
     * 校验镜像
     *
     * @param leoJob
     * @param cr
     * @param buildConfig
     * @param dsConfig
     */
    protected abstract void handleVerifyImage(LeoJob leoJob, LeoJobModel.CR cr, LeoBuildModel.BuildConfig buildConfig, T dsConfig);

    /**
     * 创建仓库
     * @param leoJob
     * @param cr
     * @param dsConfig
     */
    protected abstract void handleCreateRepository(LeoJob leoJob, LeoJobModel.CR cr, T dsConfig);

    protected abstract T getDsConfigByUuid(String uuid);

    protected T getDsConfigByUuid(String uuid, Class<T> targetClass) {
        DatasourceConfig dsConfig = dsConfigHelper.getConfigByInstanceUuid(uuid);
        return dsConfigHelper.build(dsConfig, targetClass);
    }

    /**
     * 查询云
     *
     * @param cr
     * @return
     */
    private String getDsInstanceUuid(LeoJobModel.CR cr) {
        return Optional.of(cr)
                .map(LeoJobModel.CR::getCloud)
                .map(LeoJobModel.Cloud::getUuid)
                .orElseGet(() -> getDsInstanceUuidWithName(cr));
    }

    private String getDsInstanceUuidWithName(LeoJobModel.CR cr) {
        final String name = Optional.of(cr)
                .map(LeoJobModel.CR::getCloud)
                .map(LeoJobModel.Cloud::getName)
                .orElseThrow(() -> new LeoBuildException("任务配置不存在无法验证镜像是否推送成功: job.cr.cloud.name"));
        DatasourceInstance dsInstance = instanceHelper.getInstanceByName(name);
        if (dsInstance == null)
            throw new LeoBuildException("任务CR数据源实例不存在无法验证镜像是否推送成功！");
        return dsInstance.getUuid();
    }

    public abstract String getCrType();

    /**
     * 注册
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        CrValidatorFactory.register(this);
    }

}
