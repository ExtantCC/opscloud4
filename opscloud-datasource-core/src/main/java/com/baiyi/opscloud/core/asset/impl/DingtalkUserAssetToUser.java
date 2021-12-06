package com.baiyi.opscloud.core.asset.impl;

import com.baiyi.opscloud.common.util.EmailUtil;
import com.baiyi.opscloud.core.asset.impl.base.AbstractAssetToBO;
import com.baiyi.opscloud.domain.types.BusinessTypeEnum;
import com.baiyi.opscloud.domain.types.DsAssetTypeEnum;
import com.baiyi.opscloud.domain.vo.business.BusinessAssetRelationVO;
import com.baiyi.opscloud.domain.vo.datasource.DsAssetVO;
import com.baiyi.opscloud.domain.vo.user.UserVO;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/11/30 6:23 下午
 * @Version 1.0
 */
@Component
public class DingtalkUserAssetToUser extends AbstractAssetToBO {

    @Override
    public String getAssetType() {
        return DsAssetTypeEnum.DINGTALK_USER.getType();
    }

    protected BusinessAssetRelationVO.IBusinessAssetRelation toBO(DsAssetVO.Asset asset, BusinessTypeEnum businessTypeEnum) {
        return UserVO.User.builder()
                .username(EmailUtil.toUsername(asset.getAssetKey2()))
                .displayName(asset.getName())
                .email(asset.getAssetKey2())
                .phone(asset.getProperties().get("mobile"))
                .build();
    }

    @Override
    public List<BusinessTypeEnum> getBusinessTypes() {
        return Lists.newArrayList(BusinessTypeEnum.USER);
    }

}