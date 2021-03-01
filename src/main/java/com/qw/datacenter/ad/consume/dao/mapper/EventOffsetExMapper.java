package com.qw.datacenter.ad.consume.dao.mapper;

import com.qw.datacenter.ad.consume.dao.model.EventOffset;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 */
public interface EventOffsetExMapper {
    void saveOffset(List<EventOffset> offsets);
}
