package com.eastrobot.kbs.media.plugin;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.IOException;

/**
 * ScheduleTask
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-25 9:43
 */
@Slf4j
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "convert", name = "clean-tmp", havingValue = "true")
public class ScheduleTask {

    /**
     * 同步上传的文件夹
     */
    @Value("${convert.sync.output-folder}")
    private String SYNC_OUTPUT_FOLDER;
    /**
     * 异步上传的文件夹
     */
    @Value("${convert.async.output-folder}")
    private String ASYNC_OUTPUT_FOLDER;

    /**
     * 每周日1:00am 删除临时文件
     */
    @Scheduled(cron = "0 0 1 ? * SUN")
    public void deleteTempFile() {
        try {
            FileUtils.deleteDirectory(new File(SYNC_OUTPUT_FOLDER));
            FileUtils.deleteDirectory(new File(ASYNC_OUTPUT_FOLDER));
        } catch (IOException e) {
            log.warn("删除临时文件目录失败.");
        }
    }

}
