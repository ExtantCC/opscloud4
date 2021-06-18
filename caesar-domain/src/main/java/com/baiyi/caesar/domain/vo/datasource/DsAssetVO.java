package com.baiyi.caesar.domain.vo.datasource;

import com.baiyi.caesar.domain.vo.base.BaseVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author baiyi
 * @Date 2021/6/18 5:25 下午
 * @Version 1.0
 */
public class DsAssetVO {

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    @ApiModel
    public static class Asset extends BaseVO {

        private Integer id;

        private Integer parentId;

        private String instanceUuid;

        private String name;

        private String assetId;

        private String assetType;

        private String kind;

        private String version;

        private Boolean isActive;

        private String assetKey;

        private String assetKey2;

        private String zone;

        private String regionId;

        private String assetStatus;

        private Date createdTime;

        private Date expiredTime;

        private String description;

    }
}