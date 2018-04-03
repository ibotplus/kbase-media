package com.eastrobot.converter.service;

/**
 * AudioService
 *
 * @author <a href="yogurt.lei@xiaoi.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 15:14
 */
public interface AudioService {

    /**
     * ffmpeg视频分段提取音频流(pcm格式)
     * <pre>
     *     ffmpeg -y -i {input.wav|.mp3} -ss {startTime} -t {cutTime} -acodec pcm_s16le -f s16le -ac 1 -ar 16000 {output.pcm}
     * </pre>
     *
     * @author Yogurt_lei
     * @date 2018-03-26 14:39
     */
    Boolean runFfmpegParseAudiosCmd(final String videoPath);

    /**
     *
     * 解析音频(pcm格式) 生成文本
     *
     * @author Yogurt_lei
     * @date 2018-03-27 11:54
     */
    String handle(String audioFilePath);
}
