package com.baiyi.opscloud.ansible;

import com.baiyi.opscloud.algorithm.BaseAlgorithm;
import com.baiyi.opscloud.algorithm.ServerPack;
import com.baiyi.opscloud.common.config.CachingConfig;
import com.baiyi.opscloud.domain.generator.opscloud.ServerGroup;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 服务器算法
 *
 * @Author baiyi
 * @Date 2021/5/28 1:28 下午
 * @Version 1.0
 */
@Component
public class ServerGroupingAlgorithm extends BaseAlgorithm {

    /**
     * 取服务器分组map，不含重复的主机分组模式
     * server-pord-1
     * server-pord-2
     * server-pord(不包含)
     *
     * @param serverGroup
     * @return
     */
    @Cacheable(cacheNames = CachingConfig.Repositories.SERVER, key = "'server_grouping_algorithm_servergroupid_' + #serverGroup.id", unless = "#result == null")
    public Map<String, List<ServerPack>> grouping(ServerGroup serverGroup) {
        Map<String, List<ServerPack>> serverMap = groupingByEnv(serverGroup);
        if (serverMap.isEmpty()) return serverMap;
        int subgroup = 2; // 分2组
        Set<String> keSet = Sets.newHashSet(serverMap.keySet());
        keSet.forEach(k -> {
            List<ServerPack> servers = serverMap.get(k);
            if (servers.size() >= 2) {
                groupingSubgroup(serverMap, servers, k, subgroup);
                serverMap.remove(k);
            }
        });
        return serverMap;
    }

    @CacheEvict(cacheNames = CachingConfig.Repositories.SERVER, key = "'server_grouping_algorithm_servergroupid_' + #serverGroupId")
    public void evictGrouping(Integer serverGroupId) {
    }

    private void groupingSubgroup(Map<String, List<ServerPack>> serverMap, List<ServerPack> servers, String groupingName, int subgroup) {
        List<ServerPack> preServerList = Lists.newArrayList(servers);
        // 服务器数量少于分组数量也只分2组
        if (subgroup > preServerList.size())
            subgroup = 2;
        // 每组平均服务器数量
        int size = preServerList.size() / subgroup;
        int compensate = preServerList.size() % subgroup;
        int i = 1;
        while (!preServerList.isEmpty()) {
            List<ServerPack> subServerList = acqSubgroup(preServerList, compensate >= 1 ? size + 1 : size);
            serverMap.put(Joiner.on("-").join(groupingName, i), subServerList);
            compensate--;
            i++;
        }
    }

    /**
     * 取子组
     *
     * @param serverList 服务器列表
     * @param size       数量
     * @return
     */
    private List<ServerPack> acqSubgroup(List<ServerPack> serverList, int size) {
        List<ServerPack> subList = Lists.newArrayList(serverList.subList(0, size));
        serverList.subList(0, size).clear();
        return subList;
    }

}