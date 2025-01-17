package com.baiyi.opscloud.domain.vo.leo;

import com.baiyi.opscloud.domain.vo.base.ReadableTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author baiyi
 * @Date 2022/12/6 19:56
 * @Version 1.0
 */
public class LeoDeployVO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel
    public static class Deploy implements Serializable, ReadableTime.IAgo, ReadableTime.IRuntime {

        private static final long serialVersionUID = -6080138223431460692L;

        @ApiModelProperty(value = "以前")
        private String ago;

        @ApiModelProperty(value = "运行时长")
        private String runtime;

        @ApiModelProperty(value = "部署详情")
        private Object deployDetails;

        private Integer id;
        private Integer applicationId;
        private Integer jobId;
        private String jobName;
        private Integer deployNumber;
        private Integer buildId;
        private String versionName;
        private String versionDesc;
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        private Date startTime;
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        private Date endTime;
        private String deployStatus;
        private String deployResult;
        private Boolean isFinish;
        private Integer executionType;
        private String username;
        private Boolean isActive;
        private Boolean isRollback;
        private Date createTime;
        private Date updateTime;
        private String deployConfig;
        private String comment;

        @Override
        public Date getAgoTime() {
            if (this.startTime != null) {
                return this.createTime;
            }
            return new Date();
        }

    }

}