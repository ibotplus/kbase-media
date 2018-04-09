package com.eastrobot.converter.util.ffmpeg;

import com.eastrobot.converter.util.SystemUtils;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
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
    public static double getDuration(String filePath) {
        try {
            FFprobe ffprobe = new FFprobe(filePath);
            FFmpegProbeResult probeResult = ffprobe.probe(filePath);
            return probeResult.getFormat().duration;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     *
     * 切割百度asr可用音频 长度59s
     * ffmpeg -y -i {input.wav|.mp3} -ss {startOffset} -t {duration} -acodec pcm_s16le -f s16le -ac 1 -ar 16000 {output.pcm}
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
}
/**
 * Video Encoding
 FFmpeg ffmpeg = new FFmpeg("/path/to/ffmpeg");
 FFprobe ffprobe = new FFprobe("/path/to/ffprobe");

 FFmpegBuilder builder = new FFmpegBuilder()

 .setInput("input.mp4")     // Filename, or a FFmpegProbeResult
 .overrideOutputFiles(true) // Override the output if it exists

 .addOutput("output.mp4")   // Filename for the destination
 .setFormat("mp4")        // Format is inferred from filename, or can be set
 .setTargetSize(250_000)  // Aim for a 250KB file

 .disableSubtitle()       // No subtiles

 .setAudioChannels(1)         // Mono audio
 .setAudioCodec("aac")        // using the aac codec
 .setAudioSampleRate(48_000)  // at 48KHz
 .setAudioBitRate(32768)      // at 32 kbit/s

 .setVideoCodec("libx264")     // Video using x264
 .setVideoFrameRate(24, 1)     // at 24 frames per second
 .setVideoResolution(640, 480) // at 640x480 resolution

 .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
 .done();

 FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

 // Run a one-pass encode
 executor.createJob(builder).run();

 // Or run a two-pass encode (which is slower at the cost of better quality)
 executor.createTwoPassJob(builder).run();
 **/
/**
 Get Media Information

 FFprobe ffprobe = new FFprobe("/path/to/ffprobe");
 FFmpegProbeResult probeResult = ffprobe.probe("input.mp4");

 FFmpegFormat format = probeResult.getFormat();
 System.out.format("%nFile: '%s' ; Format: '%s' ; Duration: %.3fs",
 format.filename,
 format.format_long_name,
 format.duration
 );

 FFmpegStream stream = probeResult.getStreams().get(0);
 System.out.format("%nCodec: '%s' ; Width: %dpx ; Height: %dpx",
 stream.codec_long_name,
 stream.width,
 stream.height
 );
 **/