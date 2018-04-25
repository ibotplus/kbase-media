package com.eastrobot.converter.plugin;

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
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "convert", name = "clean-tmp.enable", havingValue = "true")
public class ScheduleTask {

    @Value("${convert.outputFolder}")
    private String OUTPUT_FOLDER;

    /**
     * 每周日1:00am 删除临时文件
     */
    @Scheduled(cron = "0 0 1 ? * SUN")
    public void deleteTempFile() throws IOException {
        FileUtils.deleteDirectory(new File(OUTPUT_FOLDER));
    }

}
