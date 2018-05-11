package com.eastrobot.converter.plugin;

import com.eastrobot.converter.model.Constants;
import com.eastrobot.converter.model.FileType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@ConditionalOnBean(AsyncMode.class)
public class FileMonitor {

    /**
     * 异步上传的文件夹
     */
    @Value("${convert.async.output-folder}")
    private String ASYNC_OUTPUT_FOLDER;

    @PostConstruct
    public void init() throws Exception {
        log.warn("*************file monitor is starting*************");
        // 默认轮询5s
        long interval = TimeUnit.SECONDS.toMillis(5);
        IOFileFilter filter = FileFilterUtils.and(FileFilterUtils.fileFileFilter());
        FileAlterationObserver observer = new FileAlterationObserver(ASYNC_OUTPUT_FOLDER, filter);
        observer.addListener(new FileListener());
        // 开始监控
        FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
        monitor.start();
        log.warn("*************file monitor is running*************");
    }

    private class FileListener extends FileAlterationListenerAdaptor {
        @Override
        public void onFileCreate(File file) {
            String absolutePath = file.getAbsolutePath();
            String extension = FilenameUtils.getExtension(absolutePath);

            // 后缀是存放结果的文件或zip文件不处理
            if (FileType.RS.getExtension().equals(extension) || FileType.ZIP.getExtension().equals(extension)) {
                return;
            }
            RocketMQProducer.sendMessage(Constants.MQ_CREATE_FILE_TOPIC, Constants.MQ_CREATE_FILE_TAG, file.getAbsolutePath());
        }
    }
}
