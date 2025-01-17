package com.baiyi.opscloud.leo.delegate;

import com.baiyi.opscloud.common.config.CachingConfiguration;
import com.baiyi.opscloud.common.datasource.GitLabConfig;
import com.baiyi.opscloud.datasource.gitlab.driver.GitLabProjectDriver;
import com.baiyi.opscloud.domain.vo.leo.LeoBuildVO;
import com.baiyi.opscloud.leo.converter.GitLabBranchConverter;
import com.baiyi.opscloud.leo.exception.LeoBuildException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Tag;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @Author baiyi
 * @Date 2022/11/9 20:39
 * @Version 1.0
 */
@Slf4j
@Component
public class GitLabRepoDelegate {

    public static final String BRANCHES = "Branches";
    public static final String TAGS = "Tags";

    /**
     * 默认创建分支（发布分支）
     */
    public static final List<String> DEF_BRANCHES = Lists.newArrayList("dev", "daily", "pre", "gray", "master");

    /**
     * 生成GitLab分支选项
     *
     * @param gitlab
     * @param projectId
     * @param openTag
     * @return
     */
    @Cacheable(cacheNames = CachingConfiguration.Repositories.CACHE_FOR_10S, key = "'url_' + #gitlab.url + '_projectId_' + #projectId + '_openTag_' + #openTag")
    public LeoBuildVO.BranchOptions generatorGitLabBranchOptions(GitLabConfig.Gitlab gitlab, Long projectId, boolean openTag) {
        List<LeoBuildVO.Option> options = Lists.newArrayList();
        try {
            // Branches option
            List<Branch> GitLabBranches = GitLabProjectDriver.getBranchesWithProjectId(gitlab, projectId);
            List<LeoBuildVO.BranchOrTag> branches = GitLabBranchConverter.toBranches(GitLabBranches);
            options.add(GitLabBranchConverter.toOption(BRANCHES, branches));
            // Tags option
            if (openTag) {
                List<Tag> gitLabTags = GitLabProjectDriver.getTagsWithProjectId(gitlab, projectId);
                List<LeoBuildVO.BranchOrTag> tags = GitLabBranchConverter.toTags(gitLabTags);
                options.add(GitLabBranchConverter.toOption(TAGS, tags));
            }
            return LeoBuildVO.BranchOptions.builder()
                    .options(options)
                    .build();
        } catch (GitLabApiException e) {
            log.warn("查询GitLab branches tags: url={}, projectId={}, err={}", gitlab.getUrl(), projectId, e.getMessage());
            return LeoBuildVO.BranchOptions.EMPTY_OPTIONS;
        }
    }

    public LeoBuildVO.BranchOptions createGitLabBranch(GitLabConfig.Gitlab gitlab, Long projectId, String ref) {
        for (String branch : DEF_BRANCHES) {
            try {
                GitLabProjectDriver.createBranch(gitlab, projectId, branch, ref);
            } catch (GitLabApiException e) {
                log.warn("创建GitLab分支错误: url={}, projectId={}, branch={}, ref={}, err={}", gitlab.getUrl(), projectId, branch, ref, e.getMessage());
            }
        }
        return generatorGitLabBranchOptions(gitlab, projectId, false);
    }

    public Commit getBranchOrTagCommit(GitLabConfig.Gitlab gitlab, Long projectId, String branchNameOrTagName) {
        try {
            Optional<Branch> optionalBranch = GitLabProjectDriver.getBranchWithProjectIdAndBranchName(gitlab, projectId, branchNameOrTagName);
            if (optionalBranch.isPresent())
                return optionalBranch.get().getCommit();

            Optional<Tag> optionalTag = GitLabProjectDriver.getTagWithProjectIdAndTagName(gitlab, projectId, branchNameOrTagName);
            if (optionalTag.isPresent())
                return optionalTag.get().getCommit();
        } catch (GitLabApiException e) {
            log.error(e.getMessage());
        }
        throw new LeoBuildException("查询构建分支Commit错误: gitlab={}, projectId={}, branchOrTag={}", gitlab.getUrl(), projectId, branchNameOrTagName);
    }

}
