package com.eastrobot.kbs.media.service.impl;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * split text task
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2019-06-06 11:05
 */
public class SplitTextTask extends RecursiveTask<List<String>> {
    private String text;
    private int from;
    private int to;
    private int maxLength;

    SplitTextTask(String text, int from, int to, int maxLength) {
        this.text = text;
        this.from = from;
        this.to = to;
        this.maxLength = maxLength;
    }

    @Override
    protected List<String> compute() {
        if (to - from < maxLength) {
            return Lists.newArrayList(StringUtils.substring(text, from, to));
        } else {
            int middle = (from + to) / 2;
            SplitTextTask taskLeft = new SplitTextTask(text, from, middle, maxLength);
            SplitTextTask taskRight = new SplitTextTask(text, middle, to, maxLength);
            taskLeft.fork();
            taskRight.fork();
            ArrayList<String> list = Lists.newArrayList();
            List<String> left = taskLeft.join();
            List<String> right = taskRight.join();
            list.addAll(left);
            list.addAll(right);
            return list;
        }
    }
}