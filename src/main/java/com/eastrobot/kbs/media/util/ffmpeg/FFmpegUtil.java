package com.eastrobot.kbs.media.util.ffmpeg;

import com.eastrobot.kbs.media.model.FileExtensionType;
import com.eastrobot.kbs.media.util.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static com.eastrobot.kbs.media.model.FileExtensionType.*;

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

    private static FFprobe ffprobe;

    private static FFmpegExecutor fFmpegExecutor;

    @PostConstruct
    private void init() {
        try {
            ffprobe = new FFprobe(path + "ffprobe");
            fFmpegExecutor = new FFmpegExecutor(new FFmpeg(path + "ffmpeg"), ffprobe);
            log.info("initialize ffmpeg tools complete.");
        } catch (IOException e) {
            log.error("initialize ffmpeg occurred error !", e);
        }
    }

    /**
     * 切割音频为pcm 长度segDuration
     * ffmpeg -y -i {input.wav|.mp3} -ss {startOffset} -t {duration} -acodec pcm_s16le -f s16le -ac 1 -ar 16000 {
     * output.pcm}
     * <p>
     * pcm文件 or 存放切割pcm文件夹 (当前文件夹下)
     *
     * @param filePath    文件路径 audio|video 通用
     * @param segDuration 分段文件持续时间
     *
     * @author Yogurt_lei
     * @date 2018-04-09 18:49
     */
    public static void splitSegFileToPcm(String filePath, long segDuration) throws IOException {
        // 获取文件可播放时长
        double duration = ffprobe.probe(filePath).getFormat().duration;
        if (duration >= segDuration) {
            int totalSegment = (int) (duration / segDuration + ((duration % segDuration) > 0 ? 1 : 0));
            log.debug("total segment : {}, total second: {}", totalSegment, duration);
            for (int i = 1; i <= totalSegment; i++) {
                fFmpegExecutor
                        .createJob(new FFmpegBuilder()
                                .addInput(filePath)
                                .overrideOutputFiles(true)
                                .addOutput(Paths.get(FilenameUtils.getFullPath(filePath),
                                        FilenameUtils.getBaseName(filePath) + "-" + i + PCM.pExt()).toString())
                                .disableVideo()
                                .disableSubtitle()
                                .setStartOffset((i - 1) * segDuration, TimeUnit.SECONDS)
                                .setDuration(segDuration - 1, TimeUnit.SECONDS)
                                .setAudioCodec("pcm_s16le")
                                .setFormat("s16le")
                                .setAudioChannels(1)
                                .setAudioSampleRate(16000)
                                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                                .done()
                        ).run();
            }
        } else {
            // 非pcm格式转成pcm格式
            if (!PCM.ext().equalsIgnoreCase(FilenameUtils.getExtension(filePath))) {
                FFmpegUtil.transformAudio(filePath, PCM);
                //删除原始文件
                FileUtils.deleteQuietly(Paths.get(filePath).toFile());
            }
        }
    }

    /**
     * 音频转格式 or 视频提取音频
     * pcm 要特殊转码  其他的直接复制音轨
     *
     * @param filePath 输入文件路径
     * @param fileType 目标文件类型 {@link FileExtensionType}
     *
     * @author Yogurt_lei
     * @date 2018-04-13 10:56
     */
    public static void transformAudio(String filePath, FileExtensionType fileType) {
        FFmpegOutputBuilder outputBuilder = new FFmpegBuilder()
                .addInput(filePath)
                .overrideOutputFiles(true)
                .addOutput(
                        Paths.get(
                                ResourceUtil.ofFileNameFolder(filePath),
                                FilenameUtils.getBaseName(filePath) + fileType.pExt()
                        ).toString()
                )
                .disableVideo()
                .disableSubtitle()
                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL);
        if (PCM.equals(fileType)) {
            outputBuilder.setAudioCodec("pcm_s16le")
                    .setFormat("s16le")
                    .setAudioChannels(1)
                    .setAudioSampleRate(16000);
        } else if (AAC.equals(fileType)) {
            outputBuilder.setAudioCodec("aac");
        } else {
            outputBuilder.setAudioCodec("copy");
        }
        try {
            fFmpegExecutor.createJob(outputBuilder.done()).run();
        } catch (Exception e) {
            log.error("transformAudio occurred exception: {}" + e.getMessage());
        }
    }

    /**
     * 视频抽取帧图像(%005d.jpg) 按fps抽取,存放入以视频文件名作为文件夹的的路径下
     *
     * @param videoPath 视频文件路径
     * @param fps       fps  (e.g 0.2fps = 5s/帧)
     *
     * @author Yogurt_lei
     * @date 2018-04-13 11:36
     */
    public static void extractFrameImage(String videoPath, double fps) {
        String folder = ResourceUtil.ofFileNameFolder(videoPath);
        fFmpegExecutor.createJob(new FFmpegBuilder()
                .addInput(videoPath)
                .overrideOutputFiles(true)
                .addOutput(Paths.get(folder, "%005d" + JPG.pExt()).toString())
                .disableVideo()
                .disableSubtitle()
                .disableAudio()
                .setVideoFrameRate(fps)
                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                .done()
        ).run();
    }
}