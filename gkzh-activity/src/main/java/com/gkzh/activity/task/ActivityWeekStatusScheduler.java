package com.gkzh.activity.task;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;

/**
 * 活动状态自动切换任务
 */
@Component
public class ActivityWeekStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(ActivityWeekStatusScheduler.class);

    @Autowired
    private IActivityWeekService activityWeekService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void autoUpdateStatus() {
        List<GkzhActivityWeekInstance> instances = activityWeekService.listInstances(null);
        Date now = new Date();
        for (GkzhActivityWeekInstance instance : instances) {
            if ("3".equals(instance.getStatus())) {
                continue;
            }
            Date start = instance.getStartTime();
            Date end = instance.getEndTime();
            if (start == null || end == null) {
                continue;
            }
            String targetStatus;
            if (now.before(start)) {
                targetStatus = "0";
            } else if (!now.before(end)) {
                targetStatus = "2";
            } else {
                targetStatus = "1";
            }
            if (!targetStatus.equals(instance.getStatus())) {
                instance.setStatus(targetStatus);
                try {
                    activityWeekService.saveInstance(instance);
                } catch (Exception e) {
                    log.warn("活动状态自动切换失败: instanceId={}, targetStatus={}", instance.getInstanceId(), targetStatus, e);
                }
            }
        }
    }
}
