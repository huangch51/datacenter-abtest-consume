package com.qw.datacenter.ad.consume.service;


import com.qw.datacenter.ad.consume.dao.model.EventOffset;
import com.qw.datacenter.ad.consume.dao.service.impl.EventOffsetServiceImpl;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * kafka offsets操作类 Created by Zhan on 2017/2/7.
 */
public class SaveOffsetsOnRebalance implements ConsumerRebalanceListener {

    private static final Logger logger = LoggerFactory.getLogger(SaveOffsetsOnRebalance.class);

    private Consumer<?, ?> consumer;

    private EventOffsetServiceImpl eventOffsetService;

    private String consumerId;

    public SaveOffsetsOnRebalance(Consumer<?, ?> consumer, EventOffsetServiceImpl eventOffsetService, String consumerId) {
        this.consumer = consumer;
        this.eventOffsetService = eventOffsetService;
        this.consumerId = consumerId;
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        List<EventOffset> offsets = new ArrayList<EventOffset>();
        //将offsets保存到数据库
        for (TopicPartition partition : partitions) {
            logger.debug("onPartitionsRevoked. consumerId={}, topic={}, partition={}, offset={}",
                    consumerId, partition.topic(), partition.partition(), consumer.position(partition));
            EventOffset eo = new EventOffset();
            eo.setConsumerId(consumerId);
            eo.setTopic(partition.topic());
            eo.setPartitionId(partition.partition());
            eo.setOffsetId(consumer.position(partition));
            offsets.add(eo);
        }

        if (!offsets.isEmpty()) {
            eventOffsetService.saveAndUpdateEventOffsets(offsets);
        }
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        List<TopicPartition> partitionList = new ArrayList<>();
        //从数据库中读取offsets
        for (TopicPartition partition : partitions) {
            EventOffset eventOffset = eventOffsetService.getEventOffset(consumerId, partition.topic(), partition.partition());
            logger.debug("onPartitionsAssigned. topic={}, partition={}, offset={}", partition.topic(), partition.partition(), (eventOffset == null ? null : eventOffset.getOffsetId()));
            if (eventOffset != null) {
                if (eventOffset.getOffsetId() == -1) { //数据库中没有记录，则从partition最后的offset开始拉取
                    logger.info("seekToEnd.topic={},partition={}", partition.topic(), partition.partition());
                    partitionList.add(partition);
                    consumer.seekToEnd(partitionList);
                    partitionList.clear();
                } else {
                    logger.info("seek:topic={},partition={},offset={}", partition.topic(), partition.partition(), eventOffset.getOffsetId());
                    consumer.seek(partition, eventOffset.getOffsetId() + 1);
                }
            }
        }
    }

}


