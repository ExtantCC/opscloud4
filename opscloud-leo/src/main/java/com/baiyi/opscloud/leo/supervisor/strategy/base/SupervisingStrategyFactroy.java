package com.baiyi.opscloud.leo.supervisor.strategy.base;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author baiyi
 * @Date 2022/12/13 16:05
 * @Version 1.0
 */
@Slf4j
public class SupervisingStrategyFactroy {

    private SupervisingStrategyFactroy() {
    }

    static Map<String, SupervisingStrategy> context = new ConcurrentHashMap<>();

    public static SupervisingStrategy getStrategyByDeployType(String type) {
        return context.get(type);
    }

    public static void register(SupervisingStrategy bean) {
        context.put(bean.getDeployType(), bean);
        log.info("DeployingStrategyFactroy Registered: deployType={}", bean.getDeployType());
    }

}
