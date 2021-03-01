package com.qw.datacenter.ad.consume.conf;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;


/**
 * <p>
 * REDIS配置
 * </p>
 */
@Configuration
public class RedisPoolConfig {

    @Value("${spring.datasource.redis.ad.topic.ip}")
    private String AD_ADDR;

    @Value("${spring.datasource.redis.ad.topic.port}")
    private String AD_PORT;

    @Value("${spring.datasource.redis.ad.topic.passwd}")
    private String AD_PWD;

    @Primary
    @Bean(name = "adPool")
    public RedisTemplate<String, String> rtaPool1() {
        return redisTemplate(AD_ADDR, AD_PORT, AD_PWD);
    }

    public StringRedisTemplate redisTemplate(String addr, String port, String pwd) {
        //定义redis链接池
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxWaitMillis(20000);
        //配置对象
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(addr);
        config.setPort(Integer.parseInt(port));
        if (StrUtil.isNotBlank(pwd)) {
            config.setPassword(pwd);
        }
        //创建Lettuce工厂
        LettuceClientConfiguration clientConfiguration = LettucePoolingClientConfiguration.builder().poolConfig(poolConfig).build();
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfiguration);
        factory.afterPropertiesSet();
        //创建客户端并指定序列化方式
        StringRedisTemplate redisTemplate=new StringRedisTemplate();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

}
