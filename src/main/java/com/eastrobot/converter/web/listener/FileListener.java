package com.eastrobot.converter.web.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * FileListener
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-11 14:25
 */
@Slf4j
@Component
public class FileListener {

    @Value("${convert.outputFolder}")
    private String OUTPUT_FOLDER;

    @PostConstruct
    public void init() {
        log.warn("*********************************启动成功*********************************");
        // 默认轮询5s
        long interval = TimeUnit.SECONDS.toMillis(5);
        IOFileFilter filter = FileFilterUtils.and(FileFilterUtils.fileFileFilter());
        FileAlterationObserver observer = new FileAlterationObserver(OUTPUT_FOLDER, filter);
        observer.addListener(new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(File file) {
                log.warn("onFileCreate >>>>>" + file.getAbsolutePath());
            }

            @Override
            public void onFileChange(File file) {
                log.warn("onFileChange >>>>>" + file.getAbsolutePath());
            }

            @Override
            public void onFileDelete(File file) {
                log.warn("onFileDelete >>>>>" + file.getAbsolutePath());
            }
        });
        // 开始监控
        FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
        try {
            monitor.start();
            log.warn("*********************************监控中*********************************");
        } catch (Exception e) {
            log.error("FileAlterationMonitor occured error.", e);
        }
    }
}
