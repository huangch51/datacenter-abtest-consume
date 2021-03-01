package com.qw.datacenter.ad.consume.dao.clickhouse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.settings.ClickHouseProperties;

import java.util.List;

/**
 */
@Component
public class ClickHouseJdbcDao {

    @Value("${spring.datasource.clickhouse.username}")
    private String CK_USER;

    @Value("${spring.datasource.clickhouse.password}")
    private String CK_PWD;

    //CK0
    @Value("#{'${spring.datasource.clickhouse.ck0}'.split(',')}")
    private List<String> CK0_ADDR;

    //CK1
    @Value("#{'${spring.datasource.clickhouse.ck1}'.split(',')}")
    private List<String> CK1_ADDR;

    //CK2
    @Value("#{'${spring.datasource.clickhouse.ck2}'.split(',')}")
    private List<String> CK2_ADDR;

    //CK3
    @Value("#{'${spring.datasource.clickhouse.ck3}'.split(',')}")
    private List<String> CK3_ADDR;

    //CK4
    @Value("#{'${spring.datasource.clickhouse.ck4}'.split(',')}")
    private List<String> CK4_ADDR;

    //CK5
    @Value("#{'${spring.datasource.clickhouse.ck5}'.split(',')}")
    private List<String> CK5_ADDR;

    //CK6
    @Value("#{'${spring.datasource.clickhouse.ck6}'.split(',')}")
    private List<String> CK6_ADDR;

    //CK7
    @Value("#{'${spring.datasource.clickhouse.ck7}'.split(',')}")
    private List<String> CK7_ADDR;

    //CK8
    @Value("#{'${spring.datasource.clickhouse.ck8}'.split(',')}")
    private List<String> CK8_ADDR;

    //CK9
    @Value("#{'${spring.datasource.clickhouse.ck9}'.split(',')}")
    private List<String> CK9_ADDR;

    //CK10
    @Value("#{'${spring.datasource.clickhouse.ck10}'.split(',')}")
    private List<String> CK10_ADDR;

    //CK11
    @Value("#{'${spring.datasource.clickhouse.ck11}'.split(',')}")
    private List<String> CK11_ADDR;


    private JdbcTemplate getJdbcTemplate(String uri) {
        return new JdbcTemplate(getDataSource(uri));
    }

    private ClickHouseDataSource getDataSource(String uri) {
        ClickHouseProperties clickHouseProperties = new ClickHouseProperties();
        clickHouseProperties.setUser(CK_USER);
        if (CK_PWD != null && !"".equals(CK_PWD)) {
            clickHouseProperties.setPassword(CK_PWD);
        }
        ClickHouseDataSource dataSource = new ClickHouseDataSource(uri, clickHouseProperties);
        return dataSource;
    }

    /**
     * 入库分区表
     *
     * @param tableName
     * @param fields
     * @param list
     * @param partition
     */
    @Async("asyncThreadPoolTaskExecutor")
    public void insertBatchByPartition(String tableName, String fields, List<String> list, int partition) {
        StringBuffer sb = new StringBuffer();
        sb.append("insert into ").append(tableName).append(" (").append(fields).append(") values ");
        for (String data : list) {
            sb.append("(").append(data).append("),");
        }
        sb.deleteCharAt(sb.length() - 1);
        switch (partition) {
            case 0:
                insert(CK0_ADDR, sb.toString());
                break;
            case 1:
                insert(CK1_ADDR, sb.toString());
                break;
            case 2:
                insert(CK2_ADDR, sb.toString());
                break;
            case 3:
                insert(CK3_ADDR, sb.toString());
                break;
            case 4:
                insert(CK4_ADDR, sb.toString());
                break;
            case 5:
                insert(CK5_ADDR, sb.toString());
                break;
            case 6:
                insert(CK6_ADDR, sb.toString());
                break;
            case 7:
                insert(CK7_ADDR, sb.toString());
                break;
            case 8:
                insert(CK8_ADDR, sb.toString());
                break;
            case 9:
                insert(CK9_ADDR, sb.toString());
                break;
            case 10:
                insert(CK10_ADDR, sb.toString());
                break;
            case 11:
                insert(CK11_ADDR, sb.toString());
                break;
            default:
                break;
        }
    }

    private void insert(List<String> addr, String sql) {
        for (String s : addr) {
            JdbcTemplate jdbcTemplate = getJdbcTemplate(s);
            jdbcTemplate.execute(sql);
        }
    }
}
