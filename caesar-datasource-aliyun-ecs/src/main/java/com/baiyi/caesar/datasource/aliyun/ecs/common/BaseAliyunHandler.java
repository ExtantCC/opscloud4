package com.baiyi.caesar.datasource.aliyun.ecs.common;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.baiyi.caesar.common.datasource.config.AliyunDsConfig;
import org.springframework.util.StringUtils;

/**
 * @Author baiyi
 * @Date 2021/6/18 9:46 上午
 * @Version 1.0
 */
public class BaseAliyunHandler {

    public interface Query {
        int PAGE_SIZE = 50;
        int INSTANCE_PAGE_SIZE = 100; // Ecs
    }

    protected IAcsClient buildAcsClient(String regionId, AliyunDsConfig.aliyun aliyun) {
        String defRegionId = StringUtils.isEmpty(aliyun.getRegionId()) ? aliyun.getRegionId() : regionId;
        IClientProfile profile = DefaultProfile.getProfile(defRegionId, aliyun.getAccount().getAccessKeyId(), aliyun.getAccount().getSecret());
        return new DefaultAcsClient(profile);
    }


}