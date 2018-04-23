package com.eastrobot.converter.service.impl;

import com.eastrobot.converter.model.FileType;
import com.eastrobot.converter.model.ParseResult;
import com.eastrobot.converter.model.VacParseResult;
import com.eastrobot.converter.service.AudioService;
import com.eastrobot.converter.service.ImageService;
import com.eastrobot.converter.service.VideoService;
import com.eastrobot.converter.util.ResourceUtil;
import com.eastrobot.converter.util.ffmpeg.FFmpegUtil;
import com.hankcs.hanlp.HanLP;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.eastrobot.converter.model.ResultCode.*;

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

    @Value("${convert.video.vca.ffmpeg.toImage.fps}")
    private Double fps;

    /**
     * <pre>
     *     视频抽取音轨(*.mp3), 不能一步到位,ffmpeg无法获得pcm的时长
     *     视频抽图片(*.jpg),音轨解析文字,图片解析文字.
     * </pre>
     *
     * @author Yogurt_lei
     * @date 2018-03-26 18:06
     */
    @Override
    public VacParseResult handle(String videoFilePath, boolean isFrameExtractKeyword) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        //提交视频分段抽取音轨任务 视频抽取图片
        Future handleFuture = executor.submit(() -> {
            FFmpegUtil.transformAudio(videoFilePath, FileType.AAC);
            FFmpegUtil.extractFrameImage(videoFilePath, fps);
        });

        try {
            handleFuture.get();
        } catch (Exception e) {
            log.warn("parse video thread occurred exception : {}", e.getMessage());
        } finally {
            executor.shutdownNow();
        }

        return this.doParseVideo(videoFilePath, isFrameExtractKeyword);
    }

    private VacParseResult doParseVideo(final String videoPath, boolean isFrameExtractKeyword) {
        // 生成的文件 只有JPG和AAC(一个)
        String folderPath = ResourceUtil.getFolder(videoPath, "");
        // 声音 AAC文件
        final String audioFile = folderPath + FilenameUtils.getBaseName(videoPath)
                + FileType.AAC.getExtensionWithPoint();
        File dir = new File(folderPath);
        //图片 JPG文件
        File[] allImageFiles = dir.listFiles(pathname -> FilenameUtils.getExtension(pathname.getName()).equals
                (FileType.JPG.getExtension()));
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        // 总任务数门阀
        final CountDownLatch latch = new CountDownLatch(allImageFiles.length + 1);
        // 存储图片解析段-内容
        final Map<Integer, String> imageContentMap = new ConcurrentHashMap<>();
        // 存储图片解析段-关键字
        final Map<Integer, String> imageKeywordMap = new ConcurrentHashMap<>();
        // asr 解析结果
        final ParseResult asrParseResult = new ParseResult();
        AtomicBoolean hasOccurredException = new AtomicBoolean(false);
        // 存储图片解析异常信息 [seg:message]
        StringBuffer exceptionBuffer = new StringBuffer();

        //提交音轨转文字任务
        executor.submit(() -> {
            try {
                asrParseResult.update(audioService.handle(audioFile));
            } finally {
                latch.countDown();
            }
        });

        //提交图片转文字任务
        for (final File file : allImageFiles) {
            final String filepath = file.getAbsolutePath();
            //提交图片转文字任务
            executor.submit(() -> {
                // 图片的段是 00001 00002 00003
                String currentSegIndex = FilenameUtils.getBaseName(filepath);
                try {
                    ParseResult ocrResult = imageService.handle(filepath);
                    if (ocrResult.getCode().equals(SUCCESS)) {
                        String keyword = ocrResult.getKeyword();
                        String content = ocrResult.getContent();
                        log.debug("imageService convert {} result : {}", filepath, content);
                        imageKeywordMap.put(Integer.parseInt(currentSegIndex), keyword);
                        imageContentMap.put(Integer.parseInt(currentSegIndex), content);
                    } else {
                        throw new Exception(ocrResult.getMessage());
                    }
                } catch (Exception e) {
                    log.debug("convert image occurred exception: {}", e.getMessage());
                    hasOccurredException.set(true);
                    exceptionBuffer.append("[").append(currentSegIndex).append(":").append(e.getMessage()).append("]");
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            // 阻塞等待结束
            latch.await();
        } catch (Exception e) {
            hasOccurredException.set(true);
            exceptionBuffer.append("[").append(e.getMessage()).append("]");
            log.warn("imageService convert image thread occurred exception.", e);
        } finally {
            executor.shutdownNow();
        }

        // ocr 解析结果
        ParseResult ocrParseResult;

        // 2. 解析结束后 合并内容 根据frameExtractKeyword 绝定是否需要帧抽取关键字
        String imageKeyword = ResourceUtil.map2SortByKeyAndMergeWithSplit(imageKeywordMap, ",");
        String imageContent = ResourceUtil.map2SortByKeyAndMergeWithSplit(imageContentMap, "");
        if (StringUtils.isNotBlank(imageContent)) {
            if (!isFrameExtractKeyword) {
                List<String> phraseList = HanLP.extractKeyword(imageContent, 100);
                imageKeyword = ResourceUtil.list2String(phraseList, "");
            }

            if (hasOccurredException.get()) {
                ocrParseResult = new ParseResult(OCR_FAILURE, exceptionBuffer.toString(), imageKeyword, imageContent);
            } else {
                ocrParseResult = new ParseResult(SUCCESS, SUCCESS.getMsg(), imageKeyword, imageContent);
            }
        } else {
            ocrParseResult = new ParseResult(PARSE_EMPTY, PARSE_EMPTY.getMsg(), "", "");
        }

        return new VacParseResult(asrParseResult, ocrParseResult);
    }
}
