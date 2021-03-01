package com.qw.datacenter.ad.consume.service.cache;

import cn.hutool.core.util.StrUtil;
import com.qw.datacenter.ad.consume.model.RedisKeys;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;


@Component
public class TopicCache {

    @Resource(name = "adPool")
    private RedisTemplate redisTemplate;

    /**
     * 查询是否上线
     *
     * @param bizType
     * @return
     */
    public boolean isOnline(String bizType) {
        String key = RedisKeys.getOnline();
        String v = redisTemplate.opsForHash().get(key,bizType).toString();
        if (StrUtil.isNotBlank(v)) {
            if ("1".equals(v)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查询已上线业务
     *
     * @return
     */
    public Map<String, String> onlineKeyMap() {
        String key = RedisKeys.getOnline();
        return redisTemplate.opsForHash().entries(key);
    }


    /**
     * 查询业务列表
     *
     * @return
     */
    public Map<String, String> excMap() {
        String key = RedisKeys.getExc();
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 查询是需要保存业务
     *
     * @param bizType
     * @return
     */
    public boolean isExc(String bizType) {
        String key = RedisKeys.getExc();
        String v = redisTemplate.opsForHash().get(key,bizType).toString();
        if (StrUtil.isNotBlank(v)) {
            if ("0".equals(v)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取表名
     *
     * @param bizType
     * @return
     */
    public String getTableName(String bizType) {
        String key = RedisKeys.getTable();
        return redisTemplate.opsForHash().get(key,bizType).toString();
    }

    /**
     * 获取所有表
     *
     * @return
     */
    public Map<String, String> tableMap() {
        String key = RedisKeys.getTable();
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取字段
     *
     * @param bizType
     * @return
     */
    public String getFields(String bizType) {
        String key = RedisKeys.getFields();
        return redisTemplate.opsForHash().get(key,bizType).toString();
    }

    /**
     * 获取所有字段
     *
     * @return
     */
    public Map<String, String> FieldsMap() {
        String key = RedisKeys.getFields();
        return  redisTemplate.opsForHash().entries(key);
    }
}
