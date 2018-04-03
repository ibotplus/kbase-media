package com.eastrobot.converter.util.ffmpeg;

import com.alibaba.fastjson.JSONObject;
import com.eastrobot.converter.util.PropertiesUtil;

/**
 * FFmpegUtil
 *
 * @author <a href="yogurt.lei@xiaoi.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-29 11:02
 */
public class FFmpegUtil {
    public static final String MOV = "mov";
    public static final String FLV = "flv";
    public static final String AVI = "avi";
    public static final String MP4 = "mp4";
    public static final String PCM = "pcm";

    public boolean convert(String sourcePath, String targetPath, String format) {

        return false;
    }

    /**
     * 获取视频总播放时长秒数(seconds)
     *
     * @author Yogurt_lei
     * @date 2018-03-26 15:26
     */
    public static int getVideoTime(String videoPath) {
        try {
            FFprobe fFprobe = new FFprobe(PropertiesUtil.getString("ocr.ffmpeg"));
            JSONObject probe = fFprobe.probe(videoPath);
            String duration = probe.getJSONObject("format").getString("duration");
            int seconds = Math.round(Float.valueOf(duration));

            return seconds;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * 将秒表示时长转为{h:}m:s格式
     *
     * @param second 秒数时长
     *
     * @return 字符串格式时长
     */
    public static String parseTimeToString(int second) {
        int end = second % 60;
        int mid = second / 60;
        if (mid < 60) {
            return mid + ":" + end;
        } else if (mid == 60) {
            return "1:00:" + end;
        } else {
            int first = mid / 60;
            mid = mid % 60;

            return first + ":" + mid + ":" + end;
        }
    }

}
