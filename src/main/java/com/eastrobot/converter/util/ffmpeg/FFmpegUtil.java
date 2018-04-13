package com.eastrobot.converter.util.ffmpeg;

import com.eastrobot.converter.model.FFmpegFileType;
import com.eastrobot.converter.util.SystemUtils;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * FFmpegUtil
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-09 17:19
 */
@Slf4j
@Component
public class FFmpegUtil {

    @Value("${convert.video.vca.ffmpeg.path}")
    private String path;

    private static FFmpeg ffmpeg;

    private static FFprobe ffprobe;

    public static final String PCM = "pcm";

    @PostConstruct
    public void init() {
        try {
            if (SystemUtils.isLinux()) {
                ffmpeg = new FFmpeg();
                ffprobe = new FFprobe();
            } else {
                ffmpeg = new FFmpeg(path + "ffmpeg");
                ffprobe = new FFprobe(path + "ffprobe");
            }
        } catch (IOException e) {
            log.error("initialize ffmpeg tools occured error:{}!", e);
        }
    }

    /**
     *
     * 获取文件时长
     *
     * @author Yogurt_lei
     * @date 2018-04-09 17:24
     */
    public static double getDuration(String filePath) throws IOException {
        FFmpegProbeResult probeResult = ffprobe.probe(filePath);
        return probeResult.getFormat().duration;
    }

    /**
     *
     * 切割百度asr可用音频 长度59s
     * ffmpeg -y -i {input.wav|.mp3} -ss {startOffset} -t {duration} -acodec pcm_s16le -f s16le -ac 1 -ar 16000 {output.pcm}
     *
     * @param audioPath 音频文件路径
     * @param startOffset 截取开始时刻
     * @param outputFileFullPath 输出文件路径
     *
     * @author Yogurt_lei
     * @date 2018-04-09 18:49
     */
    public static void splitBaiduAsrAudio(String audioPath, long startOffset, String outputFileFullPath) {
        FFmpegBuilder builder = new FFmpegBuilder();
        builder.addInput(audioPath)
                .overrideOutputFiles(true)
                .addOutput(outputFileFullPath)
                .disableVideo()
                .setStartOffset(startOffset, TimeUnit.SECONDS)
                .setDuration(59, TimeUnit.SECONDS)
                .setAudioCodec("pcm_s16le")
                .setFormat("s16le")
                .setAudioChannels(1)
                .setAudioSampleRate(16000)
                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                .done();
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();
    }


    /**
     * 音频转格式
     *
     * @param filePath 输入文件路径
     * @param fileType 文件类型 {@link FFmpegFileType}
     *
     * @author Yogurt_lei
     * @date 2018-04-13 10:56
     */
    public static String transformAudio(String filePath, FFmpegFileType fileType) {
        FFmpegBuilder builder = new FFmpegBuilder();
        String outputPath = FilenameUtils.getFullPath(filePath) + FilenameUtils.getBaseName(filePath)
                + fileType.getExtensionWithPoint();
        FFmpegOutputBuilder outputBuilder = builder.addInput(filePath)
                .overrideOutputFiles(true)
                .addOutput(outputPath)
                .disableVideo()
                .disableSubtitle();
        if (FFmpegFileType.PCM.equals(fileType)) {
            outputBuilder.setAudioCodec("pcm_s16le")
                    .setFormat("s16le")
                    .setAudioChannels(1)
                    .setAudioSampleRate(16000);
        }
        builder = outputBuilder.setStrict(FFmpegBuilder.Strict.EXPERIMENTAL).done();
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();

        return outputPath;
    }
}