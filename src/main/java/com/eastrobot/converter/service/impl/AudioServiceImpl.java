package com.eastrobot.converter.service.impl;

import com.eastrobot.converter.service.AudioService;
import com.eastrobot.converter.util.BaiduSpeechUtils;
import com.eastrobot.converter.util.ResUtil;
import com.eastrobot.converter.util.ffmpeg.FFmpeg;
import com.eastrobot.converter.util.ffmpeg.FFmpegUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * AudioServiceImpl
 *
 * @author <a href="yogurt.lei@xiaoi.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 18:54
 */
@Service
public class AudioServiceImpl implements AudioService {
    private static final Logger logger = LoggerFactory.getLogger(AudioServiceImpl.class);

    @Value("${converter.ffmpeg}")
    private String ffmpegPath;

    @Value("${convert.tools.audio.asr}")
    private String ASR_TOOL;

    @Value("${converter.video.segment-seconds}")
    private String segmentSecondStr;

    private static final String BAIDU = "baidu";

    @Override
    public Boolean runFfmpegParseAudiosCmd(final String videoPath) {
        String folder = ResUtil.getFolder(videoPath, "");

        StringBuffer resultBuffer = new StringBuffer();
        int totalSeconds = FFmpegUtil.getVideoTime(videoPath);
        int segmentSecond = Integer.parseInt(segmentSecondStr);
        int totalSegment = totalSeconds / segmentSecond + ((totalSeconds % segmentSecond) > 0 ? 1 : 0);
        logger.debug("total segment :[%s], total second: [%s]", totalSegment, totalSeconds);

        for (int i = 1; i <= totalSegment; i++) {
            FFmpeg fFmpeg = new FFmpeg(ffmpegPath);
            fFmpeg.addParam("-y");
            fFmpeg.addParam("-i");
            fFmpeg.addParam(videoPath);
            fFmpeg.addParam("-ss");
            fFmpeg.addParam(FFmpegUtil.parseTimeToString((i - 1) * segmentSecond));
            fFmpeg.addParam("-t");
            fFmpeg.addParam("00:00:59");
            fFmpeg.addParam("-acodec");
            fFmpeg.addParam("pcm_s16le");
            fFmpeg.addParam("-f");
            fFmpeg.addParam("s16le");
            fFmpeg.addParam("-ac");
            fFmpeg.addParam("1");
            fFmpeg.addParam("-ar");
            fFmpeg.addParam("16000");
            fFmpeg.addParam(folder + FilenameUtils.getBaseName(videoPath) + "-" + i + ".pcm");
            try {
                fFmpeg.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } // for i end

        return true;
    }

    @Override
    public String handle(String audioFilePath) {
        //TODO 调用 NTE 接口实现语音提取文本
        if (BAIDU.equals(ASR_TOOL)) {
            JSONObject asr = BaiduSpeechUtils.asr(audioFilePath, "pcm", 16000);
            if (asr.getInt("err_no") == 0) {
                //success
                //数组字符串
                String result = asr.getString("result");
                result = StringUtils.substringBetween(result, "[\"", "\"]");

                return result;
            }
        }
        return "";
    }
}
