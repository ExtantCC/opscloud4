package com.baiyi.opscloud.config;

import org.quartz.Scheduler;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

/**
 * @Author 修远
 * @Date 2022/1/19 3:59 PM
 * @Since 1.0
 */

@Configuration
@AutoConfigureAfter(DatasourceConfiguration.class)
public class QuartzConfig {

    @Resource
    private DataSource dataSource;

    private Properties quartzProperties() {
        Properties prop = new Properties();
        try {
            prop.load(getClass().getResourceAsStream("/quartz.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true);
        factory.setDataSource(dataSource);
        factory.setStartupDelay(10);
        factory.setQuartzProperties(quartzProperties());
        factory.setAutoStartup(true);
        return factory;
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) throws Exception {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        scheduler.start();
        return scheduler;
    }

}
