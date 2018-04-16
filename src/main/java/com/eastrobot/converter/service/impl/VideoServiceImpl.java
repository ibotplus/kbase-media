package com.eastrobot.converter.service.impl;

import com.eastrobot.converter.model.FileType;
import com.eastrobot.converter.model.ParseResult;
import com.eastrobot.converter.model.VacParseResult;
import com.eastrobot.converter.service.AudioService;
import com.eastrobot.converter.service.ImageService;
import com.eastrobot.converter.service.VideoService;
import com.eastrobot.converter.util.ResourceUtil;
import com.eastrobot.converter.util.ffmpeg.FFmpegUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.*;

/**
 * VideoServiceImpl
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 10:18
 */
@Slf4j
@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private ImageService imageService;

    @Autowired
    private AudioService audioService;

    @Value("{video.vca.ffmpeg.toImage.fps}")
    private Double fps;

    /**
     * <pre>
     *     视频抽取音轨(*.pcm),
     *     视频抽图片(*.jpg),音轨解析文字,图片解析文字.
     * </pre>
     *
     * @author Yogurt_lei
     * @date 2018-03-26 18:06
     */
    @Override
    public VacParseResult handle(String videoFilePath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        //提交视频分段抽取音轨任务 视频抽取图片
        Future handleFuture = executor.submit(() -> {
            FFmpegUtil.transformAudio(videoFilePath, FileType.PCM);
            FFmpegUtil.extractFrameImage(videoFilePath, fps);
        });

        try {
            handleFuture.get();
        } catch (Exception e) {
            log.warn("parse video thread occurred exception : {}", e.getMessage());
        } finally {
            executor.shutdownNow();
        }

        return this.doParseVideo(videoFilePath);
    }

    private VacParseResult doParseVideo(final String videoPath) {
        // 生成的文件 只要jpg和pcm(一个)
        String folderPath = ResourceUtil.getFolder(videoPath, "");
        File dir = new File(folderPath);
        File[] allFiles = dir.listFiles(pathname -> FilenameUtils.getExtension(pathname.getName()).equals(FileType.JPG.getExtension()));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        // 总任务数门阀
        final CountDownLatch latch = new CountDownLatch(2);
        // 存储图片解析段-内容
        final ConcurrentHashMap<String, String> imageContentMap = new ConcurrentHashMap<>();
        // asr 解析结果
        final ParseResult asrParseResult = new ParseResult();
        // ocr 解析结果
        final ParseResult ocrParseResult = new ParseResult();
        // pcm 文件路径
        final File pcmFile = new File(folderPath + FilenameUtils.getBaseName(videoPath) + FileType.PCM.getExtensionWithPoint());

        //提交音轨转文字任务
        executor.submit(() -> {
            try {
                asrParseResult.updateResult(audioService.handle(pcmFile));
                log.info("audioService parse is complete");
            } finally {
                latch.countDown();
            }
        });

        //提交图片转文字任务
        executor.submit(() -> {
            try {
                ocrParseResult.updateResult(imageService.handle(allFiles));
                log.info("imageService parse is complete");
            } finally {
                latch.countDown();
            }
        });

        try {
            // 阻塞等待结束
            latch.await();
        } catch (Exception e) {
            log.warn("handle parse image or video thread occured exception : [%s]", e.getMessage());
        } finally {
            executor.shutdownNow();
        }

        return new VacParseResult(asrParseResult, ocrParseResult);
    }
}
