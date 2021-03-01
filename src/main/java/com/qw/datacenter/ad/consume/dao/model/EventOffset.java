package com.qw.datacenter.ad.consume.dao.model;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("abtest_event_offset")
public class EventOffset implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消费者id
     */
    private String consumerId;

    /**
     * 主体
     */
    private String topic;

    /**
     * 分区
     */
    private Integer partitionId;

    private Long offsetId;


}
