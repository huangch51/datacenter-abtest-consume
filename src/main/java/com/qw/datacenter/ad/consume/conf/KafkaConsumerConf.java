package com.qw.datacenter.ad.consume.conf;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * ad kafka配置
 * </p>
 */
@Configuration
public class KafkaConsumerConf {

    @Value("${abtest.kafka.bootstrap-servers}")
    private String SERVERS;

    @Value("${abtest.kafka.auto-commit-interval-ms}")
    private String AUTO_COMMIT_INTERVAL_MS;

    @Value("${abtest.kafka.session-timeout-ms}")
    private String SESSIONTIMEOUT;

    @Value("${abtest.kafka.group-id}")
    private String GROUPID;

    @Value("${abtest.kafka.max-poll-records}")
    private String MAX_POLL_RECORDS;

    @Value("${abtest.kafka.client-id.client}")
    private String CLIENT_ID;

    @Bean(name = "adClient")
    public KafkaConsumer<byte[], byte[]> adClientKafkaConsumer() {
        return new KafkaConsumer<>(clientConsumerConfigs());
    }

    public Map<String, Object> clientConsumerConfigs() {
        Map<String, Object> propsMap = new HashMap<>();
        propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, SERVERS);
        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        propsMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, AUTO_COMMIT_INTERVAL_MS);
        propsMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, SESSIONTIMEOUT);
        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, GROUPID);
        propsMap.put(ConsumerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
        //一次拉取消息数量
        propsMap.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS);
        return propsMap;
    }

}
