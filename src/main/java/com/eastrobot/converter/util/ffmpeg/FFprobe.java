package com.eastrobot.converter.util.ffmpeg;

import com.alibaba.fastjson.JSONObject;
import com.eastrobot.converter.util.SystemUtils;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.IOException;

/**
 * FFprobe
 *
 * @author <a href="yogurt.lei@xiaoi.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 21:00
 */
public class FFprobe extends FFcommon {
    private final String FFPROBE = "ffprobe";

    public FFprobe(String path) {
        super(path, new ProcessFunction());
        init();
    }

    public FFprobe(String path, ProcessFunction runFunction) {
        super(path, runFunction);
        init();
    }

    private void init() {
        if (SystemUtils.isLinux()) {
            path = FFPROBE;
        } else {
            path += File.separator + FFPROBE+ ".exe";
        }
    }

    public JSONObject probe(String mediaPath) throws IOException {
        JSONObject jsonObject = new JSONObject();
        ImmutableList.Builder<String> args = new ImmutableList.Builder<String>();

        args.add(path)
                .add(mediaPath)
                .add("-v", "quiet")
                .add("-print_format", "json")
                .add("-hide_banner")
                .add("-show_error")
                .add("-show_format")
                .add("-show_streams");

        Process p = runFunc.run(args.build());
        try {
            String result = ProcessUtils.waitFor(p, wrapInReader(p));
            logger.info(result);

            return JSONObject.parseObject(result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            p.destroy();
        }

        return jsonObject;
    }
}
