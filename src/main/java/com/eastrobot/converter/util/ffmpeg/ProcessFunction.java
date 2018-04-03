package com.eastrobot.converter.util.ffmpeg;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * RunProcessFunction
 *
 * @author <a href="yogurt.lei@xiaoi.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 21:09
 */
public class ProcessFunction {
    private static final Logger logger = LoggerFactory.getLogger(ProcessFunction.class);

    public Process run(List<String> args) throws IOException {
        logger.info("[%s]", Joiner.on(" ").join(args));
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.redirectErrorStream(true);
        return builder.start();
    }
}
