package com.baiyi.opscloud.packer.application;

import com.baiyi.opscloud.common.util.BeanCopierUtil;
import com.baiyi.opscloud.datasource.kubernetes.provider.KubernetesPodProvider;
import com.baiyi.opscloud.domain.base.SimpleBusiness;
import com.baiyi.opscloud.domain.builder.asset.AssetContainer;
import com.baiyi.opscloud.domain.constants.BusinessTypeEnum;
import com.baiyi.opscloud.domain.generator.opscloud.*;
import com.baiyi.opscloud.domain.param.IExtend;
import com.baiyi.opscloud.domain.vo.application.ApplicationResourceOperationLogVO;
import com.baiyi.opscloud.domain.vo.application.ApplicationResourceVO;
import com.baiyi.opscloud.domain.vo.datasource.DsAssetVO;
import com.baiyi.opscloud.domain.vo.tag.TagVO;
import com.baiyi.opscloud.packer.IWrapper;
import com.baiyi.opscloud.service.application.ApplicationResourceOperationLogService;
import com.baiyi.opscloud.service.datasource.DsInstanceAssetPropertyService;
import com.baiyi.opscloud.service.datasource.DsInstanceAssetRelationService;
import com.baiyi.opscloud.service.datasource.DsInstanceAssetService;
import com.baiyi.opscloud.service.datasource.DsInstanceService;
import com.baiyi.opscloud.service.tag.BusinessTagService;
import com.baiyi.opscloud.service.tag.TagService;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author baiyi
 * @Date 2022/2/18 2:01 PM
 * @Version 1.0
 */
@Component
@RequiredArgsConstructor
public class ApplicationResourcePacker implements IWrapper<ApplicationResourceVO.Resource> {

    private final KubernetesPodProvider kubernetesPodProvider;

    private final DsInstanceAssetService assetService;

    private final DsInstanceAssetPropertyService propertyService;

    private final DsInstanceService dsInstanceService;

    private final ApplicationResourceDsInstancePacker applicationResourceInstancePacker;

    private final DsInstanceAssetRelationService assetRelationService;

    private final TagService tagService;

    private final BusinessTagService businessTagService;

    private final ApplicationResourceOperationLogService operationLogService;

    private final ApplicationResourceOperationLogPacker operationLogPacker;

    /**
     * Deployment Pod
     *
     * @param resource
     */
    @Override
    public void wrap(ApplicationResourceVO.Resource resource, IExtend iExtend) {
        DatasourceInstanceAsset asset = assetService.getById(resource.getBusinessId());
        if (asset == null) return;
        String namespace = asset.getAssetKey2();
        String deployment = asset.getAssetKey();
        resource.setAsset(BeanCopierUtil.copyProperties(asset, DsAssetVO.Asset.class));
        try {
            DatasourceInstance dsInstance = dsInstanceService.getByUuid(asset.getInstanceUuid());
            if (dsInstance != null) {
                List<AssetContainer> assetContainers = kubernetesPodProvider.queryAssetsByDeployment(dsInstance.getId(), namespace, deployment);
                resource.setAssetContainers(assetContainers);
                resource.setTags(null);
            }
        } catch (NullPointerException ignored) {
        }
        wrapTags(resource, asset);
        wrapOperationLogs(resource);
        applicationResourceInstancePacker.wrap(resource);
    }

    public void wrapProperties(ApplicationResourceVO.Resource resource) {
        if (resource.getBusinessType() != BusinessTypeEnum.ASSET.getType()) return;
        DsAssetVO.Asset asset = BeanCopierUtil.copyProperties(assetService.getById(resource.getBusinessId()),DsAssetVO.Asset.class);
        Map<String, String> properties = propertyService.queryByAssetId(asset.getId())
                .stream().collect(Collectors.toMap(DatasourceInstanceAssetProperty::getName, DatasourceInstanceAssetProperty::getValue, (k1, k2) -> k1));
        asset.setProperties(properties);
        resource.setAsset(asset);
    }

    private void wrapTags(ApplicationResourceVO.Resource resource, DatasourceInstanceAsset asset) {
        resource.setTags(acqTags(asset));
    }

    public List<TagVO.Tag> acqTags(DatasourceInstanceAsset asset) {
        List<DatasourceInstanceAssetRelation> assetRelations = assetRelationService.queryTargetAsset(asset.getInstanceUuid(), asset.getId());
        if (CollectionUtils.isEmpty(assetRelations)) return Lists.newArrayList();
        Set<Integer> tagIdSet = Sets.newHashSet();
        assetRelations.stream().map(e ->
                assetService.getById(e.getTargetAssetId())).map(targetAsset -> businessTagService.queryByBusiness(SimpleBusiness.builder()
                .businessType(BusinessTypeEnum.ASSET.getType())
                .businessId(targetAsset.getId())
                .build())).filter(businessTags -> !CollectionUtils.isEmpty(businessTags)).forEach(businessTags -> businessTags.forEach(t -> tagIdSet.add(t.getTagId())));
        return tagIdSet.stream().map(tagId ->
                BeanCopierUtil.copyProperties(tagService.getById(tagId), TagVO.Tag.class)
        ).collect(Collectors.toList());
    }

    private void wrapOperationLogs(ApplicationResourceVO.Resource resource) {
        List<ApplicationResourceOperationLog> logs = operationLogService.queryByResourceId(resource.getId(), 5);
        if (CollectionUtils.isEmpty(logs)) return;
        resource.setOperationLogs(BeanCopierUtil.copyListProperties(logs, ApplicationResourceOperationLogVO.OperationLog.class)
                .stream()
                .peek(operationLogPacker::wrap)
                .collect(Collectors.toList())
        );
    }

}
