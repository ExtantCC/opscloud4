package com.baiyi.opscloud.datasource.server.impl;

import com.baiyi.opscloud.common.constant.enums.DsTypeEnum;
import com.baiyi.opscloud.datasource.server.impl.base.BaseZabbixHostServerProvider;
import com.baiyi.opscloud.domain.generator.opscloud.Server;
import com.baiyi.opscloud.domain.model.property.ServerProperty;
import com.baiyi.opscloud.domain.types.BusinessTypeEnum;
import com.baiyi.opscloud.facade.server.SimpleServerNameFacade;
import com.baiyi.opscloud.zabbix.entity.ZabbixHost;
import com.baiyi.opscloud.zabbix.datasource.ZabbixHostDatasource;
import com.baiyi.opscloud.zabbix.http.SimpleZabbixRequest;
import com.baiyi.opscloud.zabbix.http.SimpleZabbixRequestBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.baiyi.opscloud.zabbix.datasource.base.ZabbixServer.ApiConstant.HOST_IDS;
import static com.baiyi.opscloud.zabbix.datasource.base.ZabbixServer.ApiConstant.RESULT;

/**
 * @Author baiyi
 * @Date 2021/8/19 11:40 上午
 * @Version 1.0
 */
@Slf4j
@Component
public class ZabbixHostServerProvider extends BaseZabbixHostServerProvider {

    @Override
    protected void doCreate(Server server) {
        ServerProperty.Server property = getBusinessProperty(server);
        if (!isEnabled(property)) return;
        SimpleZabbixRequest request = SimpleZabbixRequestBuilder.builder()
                .method(ZabbixHostDatasource.HostAPIMethod.CREATE)
                .putParam("host", SimpleServerNameFacade.toServerName(server))
                .putParam("interfaces", buildHostInterfaceParams(server, property))
                .putParam("groups", buildHostGroupParams(configContext.get(), server))
                .putParam("templates", buildTemplatesParams(configContext.get(), property))
                .putParam("tags", buildTagsParams(server))
                //  .paramEntrySkipEmpty("macros", ZabbixUtils.buildMacrosParameter(serverAttributeMap))
                //  .paramEntrySkipEmpty("proxy_hostid", getProxyhostid(serverAttributeMap))
                .build();
        JsonNode data = call(configContext.get(), request);
        if (data.get(RESULT).get(HOST_IDS).isEmpty()) {
            log.error("ZabbixHost创建失败!");
        }
    }

    @Override
    protected void doUpdate(Server server) {
        ServerProperty.Server property = getBusinessProperty(server);
        if (!isEnabled(property)) return;
        String manageIp = getManageIp(server, property);
        ZabbixHost zabbixHost = null;
        try {
            zabbixHost = zabbixHostHandler.getByIp(configContext.get(), manageIp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (zabbixHost == null) {
            doCreate(server);
        } else {
            // 开始更新
            updateHost(server, property, zabbixHost);
        }
    }

    @Override
    protected void doDelete(Server server) {
        ServerProperty.Server property = getBusinessProperty(server);
        String manageIp = getManageIp(server, property);
        try {
            ZabbixHost zabbixHost = zabbixHostHandler.getByIp(configContext.get(), manageIp);
            if (zabbixHost == null) {
                return;
            }
            zabbixHostHandler.deleteById(configContext.get(), zabbixHost.getHostid());
            zabbixHostHandler.evictHostById(configContext.get(), zabbixHost.getHostid());
        } catch (Exception ignored) {
        }
        zabbixHostHandler.evictHostByIp(configContext.get(), manageIp);
    }

    @Override
    protected int getBusinessResourceType() {
        return BusinessTypeEnum.SERVERGROUP.getType();
    }

    @Override
    public String getInstanceType() {
        return DsTypeEnum.ZABBIX.getName();
    }

}
