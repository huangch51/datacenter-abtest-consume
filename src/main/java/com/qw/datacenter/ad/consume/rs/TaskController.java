package com.qw.datacenter.ad.consume.rs;

import cn.hutool.json.JSONObject;
import com.qw.datacenter.ad.consume.task.TaskStatus;
import com.qw.datacenter.ad.consume.task.timer.ClientKafkaConsumerTimerTask;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Controller;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 */
@Controller
@RequestMapping("/task")
public class TaskController {
    private final static Logger LOGGER = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private ClientKafkaConsumerTimerTask ttwjClientKafkaConsumerTimerTask;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @RequestMapping("/pause")
    @ResponseBody
    public String pause() {
        LOGGER.info("task pause!");
        TaskStatus.getInstance().setPause(true);
        ttwjClientKafkaConsumerTimerTask.stop();
        return "pause ...";
    }

    @RequestMapping("/start")
    @ResponseBody
    public String start() {
        LOGGER.info("task start!");
        TaskStatus.getInstance().setPause(false);
        ttwjClientKafkaConsumerTimerTask.start();
        return "start ...";
    }

    @RequestMapping("/startCli")
    @ResponseBody
    public String startCli() {
        LOGGER.info("client start!");
        ttwjClientKafkaConsumerTimerTask.start();
        return "start client...";
    }

    @RequestMapping("/pauseCli")
    @ResponseBody
    public String pauseCli() {
        LOGGER.info("client pause!");
        ttwjClientKafkaConsumerTimerTask.stop();
        return "pause client...";
    }

}
