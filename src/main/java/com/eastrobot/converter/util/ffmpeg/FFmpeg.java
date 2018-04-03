package com.eastrobot.converter.util.ffmpeg;

import com.eastrobot.converter.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * FFmpeg
 *
 * @author <a href="yogurt.lei@xiaoi.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 20:54
 */
public class FFmpeg extends FFcommon {
    private final String FFMPEG = "ffmpeg";

    final List<String> params = new ArrayList();

    public FFmpeg(String path) {
        super(path, new ProcessFunction());
        init();
    }

    public FFmpeg(String path, ProcessFunction runFunction) {
        super(path, runFunction);
        init();
    }

    private void init() {
        if (SystemUtils.isLinux()) {
            path = FFMPEG;
        } else {
            path += File.separator + FFMPEG + ".exe";
        }
        params.add(path);
    }

    /**
     * 增加参数
     */
    public FFmpeg addParam(String param) {
        params.add(param);
        return this;
    }

    public void clearParam() {
        params.clear();
    }

    public String execute() throws IOException {
        Process p = runFunc.run(params);
        try {
            String result = ProcessUtils.waitFor(p, wrapInReader(p));

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            p.destroy();
        }

        return "";
    }
}
