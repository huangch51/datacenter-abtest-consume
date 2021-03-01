package com.qw.datacenter.ad.consume;

import com.qw.datacenter.ad.consume.task.timer.ClientKafkaConsumerTimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * kafka启动类
 * </p>
 *
 */
@Component
@Order(1)
public class KafkaConsumerStartRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerStartRunner.class);

    @Autowired
    private ClientKafkaConsumerTimerTask adClientKafkaConsumerTimerTask;

    @Override
    public void run(ApplicationArguments args) {
        LOGGER.info("启动。");
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        scheduledExecutorService.scheduleWithFixedDelay(adClientKafkaConsumerTimerTask, 30, 10, TimeUnit.SECONDS);
    }
}
