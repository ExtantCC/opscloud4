package com.baiyi.opscloud.zabbix.provider;

import com.baiyi.opscloud.common.annotation.SingleTask;
import com.baiyi.opscloud.common.datasource.ZabbixConfig;
import com.baiyi.opscloud.common.constant.enums.DsTypeEnum;
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
import com.baiyi.opscloud.zabbix.convert.ZabbixUserAssetConvert;
import com.baiyi.opscloud.zabbix.entity.ZabbixUser;
import com.baiyi.opscloud.zabbix.entity.ZabbixUserGroup;
import com.baiyi.opscloud.zabbix.datasource.ZabbixUserGroupDatasource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.baiyi.opscloud.common.constant.SingleTaskConstants.PULL_ZABBIX_USER_GROUP;

/**
 * @Author <a href="mailto:xiuyuan@xinc818.group">修远</a>
 * @Date 2021/6/28 3:03 下午
 * @Since 1.0
 */

@Component
public class ZabbixUserGroupProvider extends AbstractAssetRelationProvider<ZabbixUserGroup, ZabbixUser> {

    @Resource
    private ZabbixUserGroupDatasource zabbixUserGroupDatasource;

    @Resource
    private ZabbixUserGroupProvider zabbixUserGroupProvider;

    @Override
    public String getInstanceType() {
        return DsTypeEnum.ZABBIX.name();
    }

    private ZabbixConfig.Zabbix buildConfig(DatasourceConfig dsConfig) {
        return dsConfigHelper.build(dsConfig, ZabbixConfig.class).getZabbix();
    }

    @Override
    protected List<ZabbixUserGroup> listEntities(DsInstanceContext dsInstanceContext, ZabbixUser target) {
        ZabbixConfig.Zabbix zabbix = buildConfig(dsInstanceContext.getDsConfig());
        return zabbixUserGroupDatasource.listByUser(zabbix, target);
    }

    @Override
    protected List<ZabbixUserGroup> listEntities(DsInstanceContext dsInstanceContext) {
        return zabbixUserGroupDatasource.list(buildConfig(dsInstanceContext.getDsConfig()));
    }

    @Override
    protected ZabbixUserGroup getEntity(DsInstanceContext dsInstanceContext, UniqueAssetParam param) {
        return zabbixUserGroupDatasource.getById(buildConfig(dsInstanceContext.getDsConfig()), param.getAssetId());
    }

    @Override
    @SingleTask(name = PULL_ZABBIX_USER_GROUP, lockTime = "5m")
    public void pullAsset(int dsInstanceId) {
        doPull(dsInstanceId);
    }

    @Override
    public String getAssetType() {
        return DsAssetTypeEnum.ZABBIX_USER_GROUP.getType();
    }

    @Override
    public String getTargetAssetKey() {
        return DsAssetTypeEnum.ZABBIX_USER.getType();
    }

    @Override
    protected boolean equals(DatasourceInstanceAsset asset, DatasourceInstanceAsset preAsset) {
        if (!AssetUtil.equals(preAsset.getName(), asset.getName()))
            return false;
        return true;
    }

    @Override
    protected AssetContainer toAssetContainer(DatasourceInstance dsInstance, ZabbixUserGroup entity) {
        return ZabbixUserAssetConvert.toAssetContainer(dsInstance, entity);
    }

    @Override
    public void afterPropertiesSet() {
        AssetProviderFactory.register(zabbixUserGroupProvider);
    }
}
