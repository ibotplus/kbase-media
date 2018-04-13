package com.eastrobot.converter.util.ffmpeg;

import com.eastrobot.converter.model.FileType;
import com.eastrobot.converter.util.ResourceUtil;
import com.eastrobot.converter.util.SystemUtils;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.eastrobot.converter.util.baidu.BaiduAsrConstants.MAX_DURATION;

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
     * 是否切割百度asr可用音频pcm 长度59s
     * ffmpeg -y -i {input.wav|.mp3} -ss {startOffset} -t {duration} -acodec pcm_s16le -f s16le -ac 1 -ar 16000 {output.pcm}
     *
     * @param filePath 文件路径 audio|video 通用
     * @return pcm文件 or 存放切割pcm文件夹 (当前文件名的文件夹下)
     *
     * @author Yogurt_lei
     * @date 2018-04-09 18:49
     */
    public static void baiduSplitSegToPcm(String filePath) throws IOException {
        double duration = FFmpegUtil.getDuration(filePath);
        String folder = ResourceUtil.getFolder(filePath, "");
        if (duration > MAX_DURATION) {
            int totalSegment = (int) (duration / MAX_DURATION + ((duration % MAX_DURATION) > 0 ? 1 : 0));
            log.debug("total segment :[%s], total second: [%s]", totalSegment, duration);
            for (int i = 1; i <= totalSegment; i++) {
                FFmpegBuilder builder = new FFmpegBuilder();
                builder.addInput(filePath)
                        .overrideOutputFiles(true)
                        .addOutput(folder + FilenameUtils.getBaseName(filePath) + "-" + i + ".pcm")
                        .disableVideo()
                        .disableSubtitle()
                        .setStartOffset((i - 1) * MAX_DURATION, TimeUnit.SECONDS)
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
        } else {
            // 不是pcm格式转成pcm格式
            if (!"pcm".equalsIgnoreCase(FilenameUtils.getExtension(filePath))) {
                FFmpegUtil.transformAudio(filePath, FileType.PCM);
                // FileUtils.deleteQuietly(new File(filePath)); //是否删除原始文件
            } else {
                FileUtils.moveFileToDirectory(new File(filePath),new File(folder),true);
            }
        }
    }

    /**
     * 音频转格式 or 视频提取音频
     *
     * @param filePath 输入文件路径
     * @param fileType 文件类型 {@link FileType}
     *
     * @author Yogurt_lei
     * @date 2018-04-13 10:56
     */
    public static void transformAudio(String filePath, FileType fileType) {
        FFmpegBuilder builder = new FFmpegBuilder();
        String folder = ResourceUtil.getFolder(filePath, "");
        String outputPath = folder + FilenameUtils.getBaseName(filePath) + fileType.getExtensionWithPoint();
        FFmpegOutputBuilder outputBuilder = builder.addInput(filePath)
                .overrideOutputFiles(true)
                .addOutput(outputPath)
                .disableVideo()
                .disableSubtitle();
        if (FileType.PCM.equals(fileType)) {
            outputBuilder.setAudioCodec("pcm_s16le")
                    .setFormat("s16le")
                    .setAudioChannels(1)
                    .setAudioSampleRate(16000);
        }
        builder = outputBuilder.setStrict(FFmpegBuilder.Strict.EXPERIMENTAL).done();
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();
    }

    /**
     * 视频抽取帧图像(.jpg) 按fps抽取 (e.g 0.2fps = 5s/帧)
     *
     * @param videoPath  视频文件路径
     * @param fps fps
     *
     * @author Yogurt_lei
     * @date 2018-04-13 11:36
     */
    public static void extractFrameImage(String videoPath, double fps) {
        String folder = ResourceUtil.getFolder(videoPath, "");
        FFmpegBuilder builder = new FFmpegBuilder();
        String outputPath = folder + File.separator + "%005d" + FileType.JPG.getExtensionWithPoint();
        builder.addInput(videoPath)
                .overrideOutputFiles(true)
                .addOutput(outputPath)
                .disableVideo()
                .disableSubtitle()
                .disableAudio()
                .setVideoFrameRate(fps)
                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                .done();
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();
    }
}