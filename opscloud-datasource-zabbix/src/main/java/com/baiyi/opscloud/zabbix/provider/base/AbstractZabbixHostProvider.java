package com.baiyi.opscloud.zabbix.provider.base;

import com.baiyi.opscloud.common.annotation.SingleTask;
import com.baiyi.opscloud.common.constant.enums.DsTypeEnum;
import com.baiyi.opscloud.common.datasource.ZabbixConfig;
import com.baiyi.opscloud.core.factory.AssetProviderFactory;
import com.baiyi.opscloud.core.model.DsInstanceContext;
import com.baiyi.opscloud.core.provider.asset.AbstractAssetRelationProvider;
import com.baiyi.opscloud.core.provider.base.param.UniqueAssetParam;
import com.baiyi.opscloud.core.util.AssetUtil;
import com.baiyi.opscloud.domain.builder.asset.AssetContainer;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceConfig;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstance;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstanceAsset;
import com.baiyi.opscloud.domain.types.DsAssetTypeEnum;
import com.baiyi.opscloud.zabbix.convert.ZabbixHostAssetConvert;
import com.baiyi.opscloud.zabbix.provider.ZabbixHostProvider;
import com.baiyi.opscloud.zabbix.v5.drive.ZabbixV5HostDrive;
import com.baiyi.opscloud.zabbix.v5.entity.ZabbixHost;

import javax.annotation.Resource;
import java.util.List;

import static com.baiyi.opscloud.common.constant.SingleTaskConstants.PULL_ZABBIX_HOST;

/**
 * @Author baiyi
 * @Date 2021/8/2 6:20 下午
 * @Version 1.0
 */
public abstract class AbstractZabbixHostProvider<T> extends AbstractAssetRelationProvider<ZabbixHost.Host, T> {

    @Resource
    protected ZabbixV5HostDrive zabbixV5HostDrive;

    @Resource
    private ZabbixHostProvider zabbixHostTargetGroupProvider;

    @Override
    public String getInstanceType() {
        return DsTypeEnum.ZABBIX.name();
    }

    protected ZabbixConfig.Zabbix buildConfig(DatasourceConfig dsConfig) {
        return dsConfigHelper.build(dsConfig, ZabbixConfig.class).getZabbix();
    }

    @Override
    protected List<ZabbixHost.Host> listEntities(DsInstanceContext dsInstanceContext) {
        return zabbixV5HostDrive.list(buildConfig(dsInstanceContext.getDsConfig()));
    }

    @Override
    protected ZabbixHost.Host getEntity(DsInstanceContext dsInstanceContext, UniqueAssetParam param) {
        return zabbixV5HostDrive.getById(buildConfig(dsInstanceContext.getDsConfig()), param.getAssetId());
    }

    @Override
    @SingleTask(name = PULL_ZABBIX_HOST, lockTime = "5m")
    public void pullAsset(int dsInstanceId) {
        doPull(dsInstanceId);
    }

    @Override
    public String getAssetType() {
        return DsAssetTypeEnum.ZABBIX_HOST.getType();
    }

    @Override
    protected boolean equals(DatasourceInstanceAsset asset, DatasourceInstanceAsset preAsset) {
        if (!AssetUtil.equals(preAsset.getName(), asset.getName()))
            return false;
        if (!AssetUtil.equals(preAsset.getAssetKey(), asset.getAssetKey()))
            return false;
        if (!AssetUtil.equals(preAsset.getKind(), asset.getKind()))
            return false;
        if (!preAsset.getIsActive().equals(asset.getIsActive()))
            return false;
        if (!AssetUtil.equals(preAsset.getDescription(), asset.getDescription()))
            return false;
        return true;
    }

    @Override
    protected AssetContainer toAssetContainer(DatasourceInstance dsInstance, ZabbixHost.Host entity) {
        return ZabbixHostAssetConvert.toAssetContainer(dsInstance, entity);
    }

    @Override
    public void afterPropertiesSet() {
        AssetProviderFactory.register(zabbixHostTargetGroupProvider);
    }
}

