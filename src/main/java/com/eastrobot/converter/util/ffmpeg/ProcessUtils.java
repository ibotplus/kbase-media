package com.eastrobot.converter.util.ffmpeg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * ProcessUtils
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-27 9:30
 */
public class ProcessUtils {
    private static final Logger logger = LoggerFactory.getLogger(ProcessUtils.class);
    /**
     * Process IO处理线程池
     */
    private static final ExecutorService PROCESS_IO_EXECUTOR = Executors.newFixedThreadPool(2);

    /**
     * ffmpeg 处理线程池
     */
    public static final ExecutorService FFMPEG_EXECUTOR = Executors.newSingleThreadExecutor();

    /**
     * 读取刷新命令行中缓冲信息 防止缓冲池数据量过大导致异常退出
     */
    private static class IOStreamThread implements Callable<String> {
        private BufferedReader br;

        public IOStreamThread(BufferedReader br) {
            this.br = br;
        }

        @Override
        public String call() {
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return sb.toString();
        }
    }

    public static String waitFor(Process p, BufferedReader br) throws Exception {
        Future<String> future = PROCESS_IO_EXECUTOR.submit(new IOStreamThread(br));
        int state = p.waitFor();
        String result = future.get();
        if (state != 0) {
            logger.warn("Process did not success complete : [%s]", result);
        }

        return result;
    }
}
