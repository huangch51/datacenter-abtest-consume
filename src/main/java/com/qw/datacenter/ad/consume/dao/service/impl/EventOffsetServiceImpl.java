package com.qw.datacenter.ad.consume.dao.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qw.datacenter.ad.consume.dao.mapper.EventOffsetExMapper;
import com.qw.datacenter.ad.consume.dao.model.EventOffset;
import com.qw.datacenter.ad.consume.dao.mapper.EventOffsetMapper;
import com.qw.datacenter.ad.consume.dao.service.IEventOffsetService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Sliven
 * @since 2019-10-12
 */
@Service
public class EventOffsetServiceImpl extends ServiceImpl<EventOffsetMapper, EventOffset> implements IEventOffsetService {
    private Logger LOGGER = LoggerFactory.getLogger(EventOffsetServiceImpl.class);

    @Resource
    private EventOffsetExMapper eventOffsetExMapper;

    public EventOffset getEventOffset(String consumerId, String topic, Integer partition) {
        LOGGER.info("getEventOffset >>>> topic = {}, partition = {}", topic, partition);
        if (StrUtil.isBlank(topic) || partition == null) {
            LOGGER.info("getEventOffset >>>> 参数为空, topic = {}, partition = {}", topic, partition);
            return null;
        }

        QueryWrapper<EventOffset> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("consumer_id", consumerId);
        queryWrapper.eq("topic", topic);
        queryWrapper.eq("partition_id", partition);
        return getOne(queryWrapper);
    }

    /**
     * @param offsets
     */
    public void saveAndUpdateEventOffsets(List<EventOffset> offsets) {
        LOGGER.debug("saveAndUpdateEventOffsets >>>> offsets = {}", JSONUtil.toJsonStr(offsets));
        eventOffsetExMapper.saveOffset(offsets);
    }
}
