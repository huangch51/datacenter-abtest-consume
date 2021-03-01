package com.qw.datacenter.ad.consume.model;

/**
 * redis key列表
 */
public class RedisKeys {

    /**
     * 业务表名映射
     *
     * @type hash field=businessId value=tableName
     */
    public static String getTable() {
        return "dc:table";
    }

    /**
     * 业务字段列表
     *
     * @return
     * @type hash field=businessId value=json(fieldList)
     */
    public static String getFields() {
        return "dc:fields";
    }

    /**
     * 该是否上线kafka消费 1上线 0 下线
     *
     * @type hash field=businessId value=1
     */
    public static String getOnline() {
        return "dc:online";
    }

    /**
     * 执行入库状态 0 .正常 1.异常 2.导入中。。
     *
     * @type hash field=businessId value=json(fieldList)
     */
    public static String getExc() {
        return "dc:exc";
    }

}
