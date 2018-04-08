package com.eastrobot.converter.util.ffmpeg;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * FFcommon
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 20:55
 */
public class FFcommon {
    protected static final Logger logger = LoggerFactory.getLogger(FFcommon.class);

    /** Path to the binary (e.g. /usr/bin/ffmpeg) */
    String path;

    /** Function to run FFmpeg. We define it like this so we can swap it out (during testing) */
    ProcessFunction runFunc;

    protected FFcommon(String path, ProcessFunction runFunction) {
        this.path = path;
        this.runFunc = runFunction;
    }

    protected BufferedReader wrapInReader(Process p) throws Exception {
        return new BufferedReader(new InputStreamReader(p.getInputStream(), "utf-8"));
    }
}
