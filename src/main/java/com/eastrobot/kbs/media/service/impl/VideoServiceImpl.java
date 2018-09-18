package com.eastrobot.kbs.media.service.impl;

import com.eastrobot.kbs.media.model.FileExtensionType;
import com.eastrobot.kbs.media.model.ParseResult;
import com.eastrobot.kbs.media.model.ResultCode;
import com.eastrobot.kbs.media.model.aitype.ASR;
import com.eastrobot.kbs.media.model.aitype.OCR;
import com.eastrobot.kbs.media.model.aitype.VAC;
import com.eastrobot.kbs.media.service.AudioService;
import com.eastrobot.kbs.media.service.ImageService;
import com.eastrobot.kbs.media.service.VideoService;
import com.eastrobot.kbs.media.util.ResourceUtil;
import com.eastrobot.kbs.media.util.concurrent.ExecutorType;
import com.eastrobot.kbs.media.util.concurrent.ThreadPoolUtil;
import com.eastrobot.kbs.media.util.ffmpeg.FFmpegUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

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
    public ParseResult<VAC> handle(String videoFilePath, boolean isFrameExtractKeyword) {
        //视频分段抽取音轨任务 视频抽取图片
        FFmpegUtil.transformAudio(videoFilePath, FileExtensionType.AAC);
        FFmpegUtil.extractFrameImage(videoFilePath, fps);

        return this.doParseVideo(videoFilePath, isFrameExtractKeyword);
    }

    private ParseResult<VAC> doParseVideo(final String videoPath, boolean isFrameExtractKeyword) {
        // 生成的文件 只有JPG(多个)和AAC(一个)
        String folderPath = ResourceUtil.getFolder(videoPath, "");
        // 声音 AAC文件
        final String audioFile = folderPath + FilenameUtils.getBaseName(videoPath) + FileExtensionType.AAC.pExt();

        //提交ASR任务
        log.debug("submit asr task: " + audioFile);
        ExecutorService executor = ThreadPoolUtil.ofExecutor(ExecutorType.GENERIC_IO_INTENSIVE);
        Future<ParseResult<ASR>> asrFuture = executor.submit(() -> audioService.handle(audioFile));
        ParseResult<ASR> asrResult;
        try {
            asrResult = asrFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
            asrResult = new ParseResult<>(ResultCode.ASR_FAILURE, null);
            log.warn("asr task occurred exception: " + e.getMessage());
        }

        // 提交OCR任务
        log.debug("submit ocr task: " + folderPath);
        ParseResult<OCR> ocrResult = imageService.handleMultiFiles(folderPath, isFrameExtractKeyword);

        ResultCode code = ResultCode.SUCCESS;
        if (ocrResult.getCode().equals(ResultCode.OCR_FAILURE) || asrResult.getCode().equals(ResultCode.ASR_FAILURE)) {
            code = ResultCode.PART_PARSE_FAILURE;
        }

        return new ParseResult<>(code, new VAC(ocrResult.getResult(), asrResult.getResult()));
    }
}
