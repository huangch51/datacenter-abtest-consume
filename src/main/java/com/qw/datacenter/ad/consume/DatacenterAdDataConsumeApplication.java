package com.qw.datacenter.ad.consume;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
@MapperScan("com.qw.datacenter.ad.consume.dao.mapper")
public class DatacenterAdDataConsumeApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        SpringApplication.run(DatacenterAdDataConsumeApplication.class, args);
    }

}
