package com.baiyi.opscloud.leo.interceptor;

import com.baiyi.opscloud.common.base.AccessLevel;
import com.baiyi.opscloud.common.exception.auth.AuthenticationException;
import com.baiyi.opscloud.common.exception.auth.AuthorizationException;
import com.baiyi.opscloud.common.util.SessionUtil;
import com.baiyi.opscloud.domain.constants.BusinessTypeEnum;
import com.baiyi.opscloud.domain.generator.opscloud.*;
import com.baiyi.opscloud.leo.domain.model.LeoRuleModel;
import com.baiyi.opscloud.leo.exception.LeoBuildException;
import com.baiyi.opscloud.leo.exception.LeoDeployException;
import com.baiyi.opscloud.leo.exception.LeoInterceptorException;
import com.baiyi.opscloud.service.application.ApplicationService;
import com.baiyi.opscloud.service.auth.AuthRoleService;
import com.baiyi.opscloud.service.leo.LeoBuildService;
import com.baiyi.opscloud.service.leo.LeoDeployService;
import com.baiyi.opscloud.service.leo.LeoJobService;
import com.baiyi.opscloud.service.leo.LeoRuleService;
import com.baiyi.opscloud.service.sys.EnvService;
import com.baiyi.opscloud.service.user.UserPermissionService;
import com.baiyi.opscloud.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.baiyi.opscloud.common.base.Global.ENV_PROD;

/**
 * @Author baiyi
 * @Date 2022/12/29 11:05
 * @Version 1.0
 */
@Component
@RequiredArgsConstructor
public class LeoDoJobInterceptorHandler {

    private final LeoDeployService leoDeployService;

    private final LeoBuildService leoBuildService;

    private final LeoJobService leoJobService;

    private final LeoRuleService leoRuleService;

    private final AuthRoleService authRoleService;

    private final UserPermissionService userPermissionService;

    private final UserService userService;

    private final EnvService envService;

    private final ApplicationService applicationService;

    /**
     * 部署并发控制
     *
     * @param jobId
     */
    public void limitConcurrentWithDeploy(int jobId) {
        int deploying = leoDeployService.countRunningWithJobId(jobId);
        if (deploying > 0) {
            throw new LeoDeployException("部署任务执行中，请勿并发操作！");
        }
    }

    /**
     * 构建并发控制
     *
     * @param jobId
     */
    public void limitConcurrentWithBuild(int jobId) {
        int building = leoBuildService.countRunningWithJobId(jobId);
        if (building > 0) {
            throw new LeoBuildException("构建任务执行中，请勿并发操作！");
        }
    }

    /**
     * 权限校验
     *
     * @param jobId
     */
    public void verifyAuthorization(int jobId) {
        // 权限校验
        LeoJob leoJob = leoJobService.getById(jobId);
        String username = SessionUtil.getUsername();

        // 用户是平台管理员则通过
        if (isAdmin(username)) {
            return;
        }

        User user = userService.getByUsername(username);
        if (user == null || !user.getIsActive()) {
            throw new AuthenticationException("用户不存在或无效！");
        }

        // 任务级授权
        if (hasJobPermission(user, jobId)) {
            // 用户有任务授权
            return;
        }

        // 应用级授权，判断用户是否授权
        verifyApplicationPermission(user, leoJob);
    }

    private void verifyApplicationPermission(User user, LeoJob leoJob) {
        UserPermission queryParam = UserPermission.builder()
                .userId(user.getId())
                .businessType(BusinessTypeEnum.APPLICATION.getType())
                .businessId(leoJob.getApplicationId())
                .build();
        UserPermission userPermission = userPermissionService.getByUniqueKey(queryParam);
        if (userPermission == null) {
            throw new AuthorizationException("非授权用户禁止操作！");
        }
        Env env = envService.getByEnvType(leoJob.getEnvType());
        if (ENV_PROD.equalsIgnoreCase(env.getEnvName())) {
            if (!"ADMIN".equalsIgnoreCase(userPermission.getPermissionRole())) {
                throw new AuthorizationException("非应用管理员禁止操作生产环境！");
            }
        }
    }

    private boolean hasJobPermission(User user, int jobId) {
        // 任务级授权
        UserPermission queryParam = UserPermission.builder()
                .userId(user.getId())
                .businessType(BusinessTypeEnum.LEO_JOB.getType())
                .businessId(jobId)
                .build();
        UserPermission userPermission = userPermissionService.getByUniqueKey(queryParam);
        return userPermission != null;
    }

    public void verifyRule(int jobId) {
        List<LeoRule> rules = leoRuleService.queryAll();
        LeoJob leoJob = leoJobService.getById(jobId);
        Env env = envService.getByEnvType(leoJob.getEnvType());
        for (LeoRule rule : rules) {
            LeoRuleModel.RuleConfig ruleConfig = LeoRuleModel.load(rule);
            List<String> envs = Optional.ofNullable(ruleConfig)
                    .map(LeoRuleModel.RuleConfig::getRule)
                    .map(LeoRuleModel.Rule::getEnvs)
                    .orElse(Collections.emptyList());
            if (!CollectionUtils.isEmpty(envs)) {
                if (envs.stream().anyMatch(e -> e.equalsIgnoreCase(env.getEnvName()))) {
                    throw new LeoInterceptorException("当前规则禁执行: {}！", rule.getName());
                }
            }
        }
    }

    /**
     * OPS角色以上即认定为系统管理员
     *
     * @return
     */
    public boolean isAdmin(String username) {
        int accessLevel = authRoleService.getRoleAccessLevelByUsername(username);
        return accessLevel >= AccessLevel.OPS.getLevel();
    }

}
