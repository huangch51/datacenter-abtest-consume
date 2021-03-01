package com.qw.datacenter.ad.consume.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 机器码,格式 ip+时间戳yyyyMMddHHmmssSSS+4位随机字符
 *
 * @author sliven
 * @date 2019-05-10
 */
public class TrackIdUtil {
    private static Logger LOGGER = LoggerFactory.getLogger(TrackIdUtil.class);

    public static String creatTrackId() {
        StringBuffer key = new StringBuffer();
        String hostIp = HttpUtil.getHostIp();
        key.append(StrUtil.removeAll(hostIp, "."));
        DateTime dt = new DateTime();
        key.append("@").append(dt.toString(DatePattern.PURE_DATETIME_MS_PATTERN)).append(RandomUtil.randomString(4));
        return key.toString();
    }

}
