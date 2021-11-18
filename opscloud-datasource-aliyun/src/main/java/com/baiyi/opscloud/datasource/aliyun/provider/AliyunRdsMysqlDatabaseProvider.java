package com.baiyi.opscloud.datasource.aliyun.provider;

import com.aliyuncs.rds.model.v20140815.DescribeDBInstancesResponse;
import com.aliyuncs.rds.model.v20140815.DescribeDatabasesResponse;
import com.baiyi.opscloud.common.annotation.SingleTask;
import com.baiyi.opscloud.common.datasource.AliyunConfig;
import com.baiyi.opscloud.common.constant.enums.DsTypeEnum;
import com.baiyi.opscloud.datasource.aliyun.convert.RdsMysqlAssetConvert;
import com.baiyi.opscloud.datasource.aliyun.rds.mysql.AliyunRdsMysqlDatasource;
import com.baiyi.opscloud.core.factory.AssetProviderFactory;
import com.baiyi.opscloud.core.model.DsInstanceContext;
import com.baiyi.opscloud.core.provider.annotation.EnablePullChild;
import com.baiyi.opscloud.core.provider.asset.AbstractAssetRelationProvider;
import com.baiyi.opscloud.core.util.AssetUtil;
import com.baiyi.opscloud.domain.builder.asset.AssetContainer;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceConfig;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstance;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstanceAsset;
import com.baiyi.opscloud.domain.types.DsAssetTypeEnum;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static com.baiyi.opscloud.common.constant.SingleTaskConstants.PULL_ALIYUN_RDS_MYSQL_DATABASE;

/**
 * @Author baiyi
 * @Date 2021/9/30 9:31 上午
 * @Version 1.0
 */
@Component
public class AliyunRdsMysqlDatabaseProvider extends AbstractAssetRelationProvider<DescribeDatabasesResponse.Database, DescribeDBInstancesResponse.DBInstance> {

    @Resource
    private AliyunRdsMysqlDatasource aliyunRdsMysqlDatasource;

    @Resource
    private AliyunRdsMysqlDatabaseProvider aliyunRdsMysqlDatabaseProvider;

    @Override
    @EnablePullChild(type = DsAssetTypeEnum.RDS_MYSQL_DATABASE)
    @SingleTask(name = PULL_ALIYUN_RDS_MYSQL_DATABASE, lockTime = "5m")
    public void pullAsset(int dsInstanceId) {
        doPull(dsInstanceId);
    }

    private AliyunConfig.Aliyun buildConfig(DatasourceConfig dsConfig) {
        return dsConfigHelper.build(dsConfig, AliyunConfig.class).getAliyun();
    }

    @Override
    protected AssetContainer toAssetContainer(DatasourceInstance dsInstance, DescribeDatabasesResponse.Database entity) {
        return RdsMysqlAssetConvert.toAssetContainer(dsInstance, entity);
    }

    @Override
    protected boolean equals(DatasourceInstanceAsset asset, DatasourceInstanceAsset preAsset) {
        if (!AssetUtil.equals(preAsset.getName(), asset.getName()))
            return false;
        return true;
    }

    @Override
    protected List<DescribeDatabasesResponse.Database> listEntities(DsInstanceContext dsInstanceContext) {
        AliyunConfig.Aliyun aliyun = buildConfig(dsInstanceContext.getDsConfig());
        if (CollectionUtils.isEmpty(aliyun.getRegionIds()))
            return Collections.emptyList();
        List<DescribeDatabasesResponse.Database> entities = Lists.newArrayList();
        aliyun.getRegionIds().forEach(regionId -> {
            List<DescribeDBInstancesResponse.DBInstance> instances = aliyunRdsMysqlDatasource.listDbInstance(regionId, aliyun);
            if (!CollectionUtils.isEmpty(instances)) {
                instances.forEach(instance -> {
                    entities.addAll(aliyunRdsMysqlDatasource.listDatabase(regionId, aliyun, instance.getDBInstanceId()));
                });
            }
        });
        return entities;
    }

    @Override
    public String getInstanceType() {
        return DsTypeEnum.ALIYUN.name();
    }

    @Override
    public String getAssetType() {
        return DsAssetTypeEnum.RDS_MYSQL_DATABASE.name();
    }

    @Override
    public String getTargetAssetKey() {
        return DsAssetTypeEnum.RDS_MYSQL_INSTANCE.name();
    }

    @Override
    public void afterPropertiesSet() {
        AssetProviderFactory.register(aliyunRdsMysqlDatabaseProvider);
    }

    @Override
    protected List<DescribeDatabasesResponse.Database> listEntities(DsInstanceContext dsInstanceContext, DescribeDBInstancesResponse.DBInstance target) {
        AliyunConfig.Aliyun aliyun = buildConfig(dsInstanceContext.getDsConfig());
        return aliyunRdsMysqlDatasource.listDatabase(aliyun.getRegionId(), aliyun, target.getDBInstanceId());
    }

}
