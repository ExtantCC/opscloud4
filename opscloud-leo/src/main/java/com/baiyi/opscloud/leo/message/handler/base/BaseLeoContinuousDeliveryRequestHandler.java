package com.baiyi.opscloud.leo.message.handler.base;

import com.baiyi.opscloud.common.leo.session.LeoDeployQuerySessionMap;
import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.leo.message.factory.LeoContinuousDeliveryMessageHandlerFactory;
import com.baiyi.opscloud.common.leo.response.LeoContinuousDeliveryResponse;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.websocket.Session;
import java.io.IOException;

/**
 * @Author baiyi
 * @Date 2022/11/23 16:11
 * @Version 1.0
 */
@Slf4j
public abstract class BaseLeoContinuousDeliveryRequestHandler<T> implements ILeoContinuousDeliveryRequestHandler, InitializingBean {

    protected T toLeoRequestParam(String message, Class<T> targetClass) {
        return new GsonBuilder().create().fromJson(message, targetClass);
    }

    protected void sendToSession(Session session, LeoContinuousDeliveryResponse response) {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(response.toString());
            }
        } catch (IOException e) {
            log.warn(e.getMessage());
        }
    }

    protected void sendToSession(Session session, DataTable body) {
        try {
            if (session.isOpen()) {
                LeoContinuousDeliveryResponse response = LeoContinuousDeliveryResponse.builder()
                        .body(body)
                        .messageType(getMessageType())
                        .build();
                session.getBasicRemote().sendText(response.toString());
            }
        } catch (IOException e) {
            log.warn("发送会话消息错误: err={}", e.getMessage());
        }
    }

    /**
     * 取消订阅
     *
     * @param sessionId
     */
    protected void unsubscribe(String sessionId) {
        log.info("取消订阅: sessionId={}, messageType={}", sessionId, getMessageType());
        LeoDeployQuerySessionMap.removeSessionQueryMap(sessionId, getMessageType());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LeoContinuousDeliveryMessageHandlerFactory.register(this);
    }

}
