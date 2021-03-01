package com.qw.datacenter.ad.consume.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

import com.qw.datacenter.ad.consume.dao.model.EventOffset;
import com.qw.datacenter.ad.consume.dao.service.impl.EventOffsetServiceImpl;

import com.qw.datacenter.ad.consume.service.cache.TopicCache;
import com.qw.datacenter.ad.consume.task.EventWorker;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * @author Sliven
 * @date 2020-01-28
 */
@Component
public class KafkaConsumerService {

    @Autowired
    private EventOffsetServiceImpl eventOffsetService;

    @Autowired
    private EventWorker eventWorker;

    private final String BIZ = "86000001";

    @Autowired
    private TopicCache topicCache;

    public void consumerRecords(KafkaConsumer<byte[], byte[]> consumer, ConsumerRecords<byte[], byte[]> records, String consumerId, Logger logger) throws IOException {
        long startTime = System.currentTimeMillis();
        List<EventOffset> offsets = new ArrayList<>();
        int eventCounter = 0;

        //TODO 暂时写死表名和字段
        String tableName = topicCache.getTableName(BIZ);
        String fields = topicCache.getFields(BIZ);

        //不写all表
        if (StrUtil.isWrap(tableName, "", "_all")) {
            tableName = StrUtil.unWrap(tableName, "", "_all");
        }

        for (TopicPartition partition : records.partitions()) {
            List<ConsumerRecord<byte[], byte[]>> partitionRecords = records.records(partition);
            for (ConsumerRecord<byte[], byte[]> record : partitionRecords) {

                try {
                    byte[] value = record.value();
//                    if (value.length < 20) {
//                        continue;   // 数据太短，不符合zip压缩规范
//                    }
                    logger.debug("消费到数据:data={}", new String(value));
                    System.out.println(new String(value));
                    JsonIterator jsonIterator = JsonIterator.parse("["+new String(value)+"]");
                    //将map.get("esGoods")先转为json字符串，然后将得到的json字符串转为list就ok了！！！
                    List<Any> anyList = jsonIterator.readAny().asList();
                    String keyPre = DateTime.now().toString("yyyyMMdd")  + partition.partition() +  record.offset();
                    eventWorker.saveClientData(anyList, tableName, fields, keyPre, logger);
                    eventCounter++;
                } catch (Exception e) {
                    logger.error("Kafka处理异常了。topic={}, e={}", partition.topic(), e);
                }

            }
            //处理完每个partition中的消息后提交offset
            long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
            consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));

            EventOffset eventOffset = new EventOffset();
            eventOffset.setConsumerId(consumerId);
            eventOffset.setOffsetId(lastOffset);
            eventOffset.setTopic(partition.topic());
            eventOffset.setPartitionId(partition.partition());
            offsets.add(eventOffset);
        }

        //记录每个partition的偏移量
        if (!offsets.isEmpty()) {
            eventOffsetService.saveAndUpdateEventOffsets(offsets);
        }

        long endTime = System.currentTimeMillis();
        logger.info("从kafka拉取事件记录完成, 事件{}条, 用时={} ms", eventCounter, endTime - startTime);

    }

}
