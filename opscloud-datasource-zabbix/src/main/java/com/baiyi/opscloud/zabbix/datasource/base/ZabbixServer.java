package com.baiyi.opscloud.zabbix.datasource.base;

import com.baiyi.opscloud.common.datasource.ZabbixConfig;
import com.baiyi.opscloud.common.redis.RedisUtil;
import com.baiyi.opscloud.common.util.StringToDurationUtil;
import com.baiyi.opscloud.common.util.TimeUtil;
import com.baiyi.opscloud.zabbix.entity.ZabbixUser;
import com.baiyi.opscloud.zabbix.http.DefaultZabbixClient;
import com.baiyi.opscloud.zabbix.http.IZabbixRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @Author <a href="mailto:xiuyuan@xinc818.group">修远</a>
 * @Date 2021/6/25 4:21 下午
 * @Since 1.0
 */

@Component
@RequiredArgsConstructor
public class ZabbixServer {

    public static final String AUTH_CACHE_KAY_PREFIX = "opscloud_v4_zabbix_auth";

    public interface ApiConstant {
        String RESULT = "result";
        // String USER_IDS = "userids";
        // String USER_GROUP_IDS = "usrgrpids";
        String HOST_GROUP_IDS = "groupids";
        String HOST_IDS = "hostids";
        // String HOST_ID = "hostid";
        String EVENT_IDS = "eventids";
        String TEMPLATE_IDS = "templateids";
        String TRIGGER_IDS = "triggerids";
    }

    private final RedisUtil redisUtil;

    @Retryable(value = RuntimeException.class, maxAttempts = 2, backoff = @Backoff(delay = 1000, multiplier = 1.5))
    public JsonNode call(ZabbixConfig.Zabbix zabbix, IZabbixRequest request) {
        DefaultZabbixClient zabbixClient = buildZabbixClient(zabbix);
        return zabbixClient.call(request);
    }

    private DefaultZabbixClient buildZabbixClient(ZabbixConfig.Zabbix zabbix) {
        String auth = getAuth(zabbix);
        if (!StringUtils.isEmpty(auth))
            return new DefaultZabbixClient(zabbix, auth);
        DefaultZabbixClient zabbixClient = new DefaultZabbixClient(zabbix);
        auth = zabbixClient.getAuth();
        cacheAuth(zabbix, zabbixClient.getLoginUser(), auth);
        return zabbixClient;
    }

    private void cacheAuth(ZabbixConfig.Zabbix zabbix, ZabbixUser loginUser, String auth) {
        String key = Joiner.on("_").join(AUTH_CACHE_KAY_PREFIX, zabbix.getUrl());
        redisUtil.set(key, auth, getAuthCacheTime(loginUser));
    }

    private String getAuth(ZabbixConfig.Zabbix zabbix) {
        String key = Joiner.on("_").join(AUTH_CACHE_KAY_PREFIX, zabbix.getUrl());
        if (redisUtil.hasKey(key)) {
            redisUtil.get(key);
        }
        return null;
    }

    private long getAuthCacheTime(ZabbixUser zabbixUser) {
        if (1 == zabbixUser.getAutologin()) {
            // 缓存7天
            return TimeUtil.dayTime / 1000 * 7;
        }
        Duration cacheTime = StringToDurationUtil.parse(zabbixUser.getAutologout());
        return cacheTime.getSeconds();
    }

}
