package com.baiyi.opscloud.domain.generator.opscloud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "leo_build")
public class LeoBuild {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 任务ID
     */
    @Column(name = "job_id")
    private Integer jobId;

    /**
     * 任务名称
     */
    @Column(name = "job_name")
    private String jobName;

    /**
     * 应用ID
     */
    @Column(name = "application_id")
    private Integer applicationId;

    /**
     * 构建编号
     */
    @Column(name = "build_number")
    private Integer buildNumber;

    /**
     * 版本
     */
    @Column(name = "version_name")
    private String versionName;

    /**
     * 版本描述
     */
    @Column(name = "version_desc")
    private String versionDesc;

    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private Date startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private Date endTime;

    /**
     * 构建状态
     */
    @Column(name = "build_status")
    private String buildStatus;

    /**
     * 构建结果
     */
    @Column(name = "build_result")
    private String buildResult;

    /**
     * 任务是否完成
     */
    @Builder.Default
    @Column(name = "is_finish")
    private Boolean isFinish = false;

    /**
     * 执行类型
     */
    @Column(name = "execution_type")
    private Integer executionType;

    /**
     * 操作用户
     */
    private String username;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 构建配置
     */
    @Column(name = "build_config")
    private String buildConfig;

    /**
     * 描述
     */
    private String comment;
}