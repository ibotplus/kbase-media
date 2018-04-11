package com.eastrobot.converter.web.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;

/**
 * FileListener
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-11 14:25
 */
@Slf4j
public class FileListener implements FileAlterationListener {

    @Override
    public void onStart(FileAlterationObserver observer) {
        // log.warn("onStart >>>>>");
    }

    @Override
    public void onDirectoryCreate(File directory) {
        log.warn("onDirectoryCreate >>>>>" + directory.getAbsolutePath());
    }

    @Override
    public void onDirectoryChange(File directory) {
        log.warn("onDirectoryChange >>>>>" + directory.getAbsolutePath());
    }

    @Override
    public void onDirectoryDelete(File directory) {
        log.warn("onDirectoryDelete >>>>>" + directory.getAbsolutePath());
    }

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

    @Override
    public void onStop(FileAlterationObserver observer) {
        // log.warn("onStop >>>>>");
    }
}
