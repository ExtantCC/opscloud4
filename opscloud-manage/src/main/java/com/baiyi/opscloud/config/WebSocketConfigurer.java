package com.baiyi.opscloud.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.util.WebAppRootListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @Author baiyi
 * @Date 2020/3/28 12:02 上午
 * @Version 1.0
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class WebSocketConfigurer implements ServletContextInitializer {

    /**
     * 给spring容器注入这个ServerEndpointExporter对象
     * 相当于xml：
     * <beans>
     * <bean id="serverEndpointExporter" class="org.springframework.web.socket.server.standard.ServerEndpointExporter"/>
     * </beans>
     * <p>
     * 检测所有带有@serverEndpoint注解的bean并注册他们。
     *
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        servletContext.addListener(WebAppRootListener.class);
        /**
         * If the application does not define a MessageHandler.Partial for incoming text messages, any incoming text
         * messages must be buffered so the entire message can be delivered in a single call to the registered MessageHandler.Whole
         * for text messages. The default buffer size for text messages is 8192 bytes. This may be changed for a web application
         * by setting the servlet context initialization parameter org.apache.tomcat.websocket.textBufferSize to the desired value in bytes.
         */
        servletContext.setInitParameter("org.apache.tomcat.websocket.textBufferSize", "5242800");
        servletContext.setInitParameter("org.apache.tomcat.websocket.binaryBufferSize","52428800");
    }
}