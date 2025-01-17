package com.baiyi.opscloud.config;

import com.baiyi.opscloud.common.redis.RedisUtil;
import com.baiyi.opscloud.common.util.TimeUtil;
import com.baiyi.opscloud.domain.generator.opscloud.Instance;
import com.baiyi.opscloud.facade.sys.InstanceFacade;
import com.baiyi.opscloud.util.SystemInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.UnknownHostException;

/**
 * @Author baiyi
 * @Date 2023/1/29 09:54
 * @Version 1.0
 */
@Slf4j
@Component
public class InitialInstanceSystemInfo {

    @Resource
    private InstanceFacade instanceFacade;

    @Resource
    private RedisUtil redisUtil;

    @PostConstruct
    public void init() {
        try {
            Instance instance = instanceFacade.getInstance();
            if (instance == null) return;
            redisUtil.set(SystemInfoUtil.buildKey(instance), SystemInfoUtil.buildInfo(), TimeUtil.dayTime / 1000 * 365);
            log.info("初始化实例系统信息！");
        } catch (UnknownHostException ignored) {
            log.error("查询实例信息错误！");
        }
    }

}
