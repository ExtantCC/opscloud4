package com.baiyi.opscloud.facade.sys;

import com.baiyi.opscloud.domain.vo.sys.InstanceVO;

/**
 * @Author baiyi
 * @Date 2021/9/3 1:30 下午
 * @Version 1.0
 */
public interface InstanceFacade {

    boolean isHealth();

    InstanceVO.Health checkHealth();
}
