package com.qw.datacenter.ad.consume.task.timer;

import cn.hutool.core.util.StrUtil;
import com.qw.datacenter.ad.consume.dao.model.EventOffset;
import com.qw.datacenter.ad.consume.dao.service.impl.EventOffsetServiceImpl;
import com.qw.datacenter.ad.consume.service.KafkaConsumerService;
import com.qw.datacenter.ad.consume.service.SaveOffsetsOnRebalance;
import com.qw.datacenter.ad.consume.task.TaskStatus;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * @author Sliven
 * @date 2020-01-28
 */
@Component
public class ClientKafkaConsumerTimerTask extends TimerTask {

    private Logger LOGGER = LoggerFactory.getLogger(ClientKafkaConsumerTimerTask.class);
    private static boolean stop = false;
    private static final int POLL_TIMEOUT = 3000;
    private static final int POLL_INTERVAL = 2000;

    @Value("${abtest.kafka.topic.client}")
    private String TOPIC;

    @Autowired
    private EventOffsetServiceImpl eventOffsetService;

    @Value("${abtest.kafka.consumer-id.client}")
    private String CONSUMER_ID;

    @Autowired
    private KafkaConsumerService kafkaConsumerService;

    @PreDestroy
    public void stop() {
        stop = true;
    }

    public void start() {
        stop = false;
    }

    @Resource(name = "adClient")
    private KafkaConsumer<byte[], byte[]> clientKafkaConsumer;

    private void init() {

        if (StrUtil.isBlank(TOPIC)) {
            LOGGER.warn("clientKafkaConsumerTimerTask 未配置topic");
            return;
        }
        List<String> topics = new ArrayList<>();
        topics.add(TOPIC);
        clientKafkaConsumer.subscribe(topics, new SaveOffsetsOnRebalance(clientKafkaConsumer, eventOffsetService, CONSUMER_ID));
        clientKafkaConsumer.poll(Duration.ofMillis(POLL_TIMEOUT));

        List<TopicPartition> partitions=new ArrayList<>();

        //调用seek()获取partitions中的offset
        for (TopicPartition partition : clientKafkaConsumer.assignment()) {
            if (partition == null) {
                continue;
            }
            EventOffset eventOffset = eventOffsetService.getEventOffset(CONSUMER_ID, partition.topic(), partition.partition());
            if (eventOffset != null) {
                if (eventOffset.getOffsetId() == -1) {
                    LOGGER.info("init.seekToEnd.topic={},partition={}", partition.topic(), partition.partition());
                    partitions.add(partition);
                    clientKafkaConsumer.seekToEnd(partitions);
                    partitions.clear();
                } else {
                    LOGGER.info("init.seek:topic={},partition={},offset={}", partition.topic(), partition.partition(), eventOffset.getOffsetId());
                    clientKafkaConsumer.seek(partition, eventOffset.getOffsetId() + 1);
                }
            }
        }
    }

    @Override
    public void run() {

        TaskStatus status = TaskStatus.getInstance();
        String className = this.getClass().getName();

        if (status.isPause()) {
            LOGGER.warn("任务：总开关没开");
            return;
        }

        LOGGER.info("AdClientKafkaConsumerTimerTask 开始..");
        status.setTaskRunning(className);

        long sleepTime;

        try {
            init();
            while (!stop && !status.isPause()) {
                status.incTaskCount(className);
                status.setTaskStartTime(className, System.currentTimeMillis());
                ConsumerRecords<byte[], byte[]> records = clientKafkaConsumer.poll(Duration.ofMillis(POLL_TIMEOUT));
                sleepTime = records.isEmpty() ? POLL_INTERVAL : 300;
                kafkaConsumerService.consumerRecords(clientKafkaConsumer, records, CONSUMER_ID,LOGGER);
                //每拉一次数据更新一下状态
                status.setTaskDoneTime(className, System.currentTimeMillis());
                Thread.sleep(sleepTime);
            }
            LOGGER.info("优雅的结束。");

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        status.setTaskDoneTime(className, System.currentTimeMillis());
        status.setTaskDone(className);
    }

}
