package com.qw.datacenter.ad.consume.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

import com.qw.datacenter.ad.consume.dao.clickhouse.ClickHouseJdbcDao;
import com.qw.datacenter.ad.consume.utils.DateUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sliven
 * @since 2020-01-29
 */
@Service
public class EventWorker {

    @Autowired
    private ClickHouseJdbcDao clickHouseDao;

    /**
     * hash 分区数量
     */
    private static final int HASH_PARTITION = 12;

    /**
     * @param anyList
     * @param table
     * @param fields
     * @param uniqueKey
     * @param logger
     */
    @Async("bizThreadPoolTaskExecutor")
    public void saveClientData(List<Any> anyList, String table, String fields, String uniqueKey, Logger logger) {
        Map<String, List> insertMap = new HashMap<>();
        long startTime = System.currentTimeMillis();
        try {
            //转换字段为数组
            List<Any> fieldList = JsonIterator.parse(fields).readAny().asList();
            //转换字段为数组
            StringBuffer field = new StringBuffer();
            //字段转字符串
            for (Any fieldJson : fieldList) {
                field.append(fieldJson.toString("fieldName")).append(",");
            }
            //删除最后一个","
            field.deleteCharAt(field.length() - 1);


            for (int i = 0; i < anyList.size(); i++) {
                Any any = anyList.get(i);
                //先转数据到map
                Map<String, String> map = new HashMap<>();
                int part = 0;
                for (Object key : any.keys()) {
                    if ("payload".equals(key.toString())) {
                        Any payload = any.get("payload");
                        for (Object propKey : payload.keys()) {
                            if ("domain_route".equals(propKey.toString())) {
                                Any domain_route = payload.get("domain_route");
                                for (Object up_domain_list : domain_route.keys()) {
                                    map.put(up_domain_list.toString(), domain_route.toString(up_domain_list));
                                }
                            }
                            map.put(propKey.toString(), payload.toString(propKey));
                        }
                    } else {
                        map.put(key.toString(), any.toString(key));
                    }
                }

                //获取要插入的分区
                if (map.get("user_id") != null) {
                    part = Math.abs(map.get("user_id").hashCode() % HASH_PARTITION);
                }

                //转换数据成sql
                String sql = parseRecord(map, fieldList, uniqueKey, i, logger);

                //根据hash分区划分list
                List list = new ArrayList();
                if (insertMap.get(part + "") != null) {
                    list = CollUtil.newCopyOnWriteArrayList(insertMap.get(part + ""));
                }
                list.add(sql);
                insertMap.put(part + "", list);
            }

            //入库
            for (String s : insertMap.keySet()) {
                clickHouseDao.insertBatchByPartition(table, field.toString(), insertMap.get(s), Integer.parseInt(s));
                logger.info("入库完成, 分区={}, 表名={}, 事件{}条, 用时={} ms", s, table, insertMap.get(s).size(), System.currentTimeMillis() - startTime);
            }
        } catch (IOException e) {
            logger.info("转换数据失败 uniqueKey={}, e={}", uniqueKey, e);
        }
    }


    /**
     * 封装入库字段
     *
     * @param dataJson
     * @param fieldArr
     * @param uniqueKey
     * @param logger
     * @param num
     * @return
     */
    public String parseRecord(Map<String, String> dataJson, List<Any> fieldArr, String uniqueKey, int num, Logger logger) {
        //封装字段,
        StringBuffer key = new StringBuffer();
        for (Any any : fieldArr) {
            String fieldName = any.toString("fieldName");
            String dataType = any.toString("dataType");
            //替换`
            if (StrUtil.containsIgnoreCase(fieldName, "`")) {
                fieldName = StrUtil.replace(fieldName, "`", "");
            }

            //日期类型处理
            if ("5".equals(dataType)) {
                if (StrUtil.isBlank(dataJson.get(fieldName))) {
                    key.append("\'").append(formatDate(null)).append("\',");
                    continue;
                } else {
                    //格式化时间
                    try {
                        //判断时间格式
                        String dateStr = dataJson.get(fieldName);
                        if (!DateUtil.verifyDateIsDateTime(dateStr)) {
                            logger.error("时间格式错误,已更改为当前时间 field={},dateStr={},e={}", fieldName, dateStr);
                            dateStr = formatDate(null);
                        }

                        key.append("\'").append(dateStr).append("\',");
                        continue;
                    } catch (Exception e) {
                        logger.error("时间处理异常,field={},dataList={},e={}", fieldName, dataJson.toString(), e);
                        key.append("\'").append(formatDate(null)).append("\',");
                        continue;
                    }
                }
            }

            //数字类型处理
            if ("2".equals(dataType) || "3".equals(dataType) || "6".equals(dataType)) {
                if (StrUtil.isBlank(dataJson.get(fieldName))) {
                    key.append("0,");
                    continue;
                } else {
                    //格式化时间
                    try {
                        key.append(dataJson.get(fieldName)).append(",");
                        continue;
                    } catch (Exception e) {
                        logger.error("数字处理异常,field={},dataList={},e={}", fieldName, dataJson.toString(), e);
                        key.append("0,");
                        continue;
                    }
                }
            }

            if ("key".equals(fieldName)) {
                key.append("\'").append(uniqueKey).append(num).append("\',");
                continue;
            }

            if (StrUtil.isNotBlank(dataJson.get(fieldName))) {
                String tmp = dataJson.get(fieldName);
                String val = "";

                //转义特殊字符'
                //替换mybatis关键字 #{}
                if (StrUtil.containsIgnoreCase(tmp, "#{")) {
                    tmp = StrUtil.replace(tmp, "#{", "{");
                }

                //替换mybatis关键字 ${}
                if (StrUtil.containsIgnoreCase(tmp, "${")) {
                    tmp = StrUtil.replace(tmp, "${", "{");
                }

                //替换`
                if (StrUtil.containsIgnoreCase(tmp, "`")) {
                    tmp = StrUtil.replace(tmp, "`", "");
                }

                //替换'
                if (StrUtil.containsIgnoreCase(tmp, "'")) {
                    val = StrUtil.replace(tmp, "'", "''");
                    //替换\
                    if (StrUtil.containsIgnoreCase(val, "\\")) {
                        val = StrUtil.replace(val, "\\", "\\\\");
                    }
                } else if (StrUtil.containsIgnoreCase(tmp, "\\")) {
                    val = StrUtil.replace(tmp, "\\", "\\\\");
                }


                if (StrUtil.isBlank(val)) {
                    key.append("\'").append(tmp).append("\',");
                } else {
                    key.append("\'").append(val).append("\',");
                }

            } else {
                key.append("\'\'").append(",");
            }
        }

        //删除最后一个","
        key.deleteCharAt(key.length() - 1);
        return key.toString();
    }

    private String formatDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN).withZone(ZoneId.of("GMT+8"));
        if (StrUtil.isNotBlank(dateStr)) {
            return LocalDateTime.parse(dateStr, formatter).format(formatter);
        }
        return LocalDateTime.now().format(formatter);
    }

}
