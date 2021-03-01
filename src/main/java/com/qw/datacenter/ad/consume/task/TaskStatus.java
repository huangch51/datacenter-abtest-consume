package com.qw.datacenter.ad.consume.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 说明 版权 Created by fangci on 2016/11/25.
 */
public class TaskStatus {

    public final static long ONE_HOUR_MILLIS = 3600 * 1000L;

    private final static Logger LOGGER = LoggerFactory.getLogger(TaskStatus.class);

    // 最高控制，默认启动程序先暂停
    private boolean pause = true;

    // 已经停止的时间
    private long pauseStartTime = 0L;

    private Map<String, Boolean> taskRunning = new ConcurrentHashMap<>();
    private Map<String, Long> taskLastDoneTime = new ConcurrentHashMap<>();
    private Map<String, Long> taskCount = new ConcurrentHashMap<>();
    private Map<String, Long> taskStartTime = new ConcurrentHashMap<>();
    private Map<String, Long> taskUseTime = new ConcurrentHashMap<>();

    private TaskStatus() {
    }

    private static class Singleton {
        private static final TaskStatus instance = new TaskStatus();
    }

    private void avoidIdle() {
        TaskStatus status = Singleton.instance;
        if (status.isPause()) {
            if (status.getPauseStartTime() == 0) {
                status.setPauseStartTime(System.currentTimeMillis());
            } else if (System.currentTimeMillis() - status.getPauseStartTime() > ONE_HOUR_MILLIS) {
                status.setPause(false);
                status.setPauseStartTime(0);
                LOGGER.info("Avoid idle OK.");
            }
        }
    }

    public static TaskStatus getInstance() {
        TaskStatus status = Singleton.instance;
        status.avoidIdle();
        return status;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
        if (pause) {
            setPauseStartTime(System.currentTimeMillis());
        }

    }

    public boolean taskIsRunning(String className) {
        boolean ret = false;
        Boolean r = taskRunning.get(className);
        if (r == null) {
            taskRunning.put(className, false);
        } else {
            ret = r;
        }
        return ret;
    }

    public void setTaskRunning(String className) {
        taskRunning.put(className, true);
        setTaskStartTime(className, System.currentTimeMillis());
    }

    public void setTaskDone(String className) {
        taskRunning.put(className, false);
    }

    public void setTaskDoneTime(String className, long doneTime) {
        taskLastDoneTime.put(className, doneTime);
        taskUseTime.put(className, doneTime - getTaskStartTime(className));
    }

    public Long getTaskDoneTime(String className) {
        return taskLastDoneTime.get(className);
    }

    public void incTaskCount(String className) {
        Long c = taskCount.get(className);
        taskCount.put(className, c == null ? 1 : c + 1);
    }

    public long getTaskCount(String className) {
        Long c = taskCount.get(className);
        return c == null ? 0 : c;
    }

    public long getPauseStartTime() {
        return pauseStartTime;
    }

    public void setPauseStartTime(long pauseStartTime) {
        this.pauseStartTime = pauseStartTime;
    }

    public Set<String> getAllTask() {
        return taskRunning.keySet();
    }

    public void setTaskStartTime(String className, long startTime) {
        taskStartTime.put(className, startTime);
    }

    public Long getTaskStartTime(String className) {
        Long start = taskStartTime.get(className);
        return start == null ? 0 : start;
    }

    public Long getTaskUseTime(String className) {
        return taskUseTime.get(className);
    }

}
