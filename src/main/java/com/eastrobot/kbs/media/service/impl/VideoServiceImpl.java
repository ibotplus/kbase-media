package com.eastrobot.kbs.media.service.impl;

import com.eastrobot.kbs.media.model.Constants;
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
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
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
     *     视频抽取音轨(*.aac), 不能一步到位,ffmpeg无法获得pcm的时长
     *     视频抽图片(*.jpg)
     *     音轨解析文字,图片解析文字.
     * </pre>
     *
     * @author Yogurt_lei
     * @date 2018-03-26 18:06
     */
    @Override
    public ParseResult<VAC> handle(String videoFilePath, Map paramMap) {
        //视频分段抽取音轨任务 视频抽取图片
        FFmpegUtil.transformAudio(videoFilePath, FileExtensionType.AAC);
        FFmpegUtil.extractFrameImage(videoFilePath, fps);

        // 生成的文件 只有JPG(多个)和AAC(一个)
        String folderPath = ResourceUtil.ofFileNameFolder(videoFilePath);
        // 声音 AAC文件
        String audioFile = Paths.get(folderPath,
                FilenameUtils.getBaseName(videoFilePath) + FileExtensionType.AAC.pExt()).toString();

        //提交ASR任务
        log.debug("submit asr task: " + audioFile);
        ExecutorService executor = ThreadPoolUtil.ofExecutor(ExecutorType.GENERIC_IO_INTENSIVE);
        Future<ParseResult<ASR>> asrFuture = executor.submit(() -> audioService.handle(audioFile, paramMap));
        ParseResult<ASR> asrResult;
        try {
            asrResult = asrFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
            asrResult = new ParseResult<>(ResultCode.ASR_FAILURE, null);
            log.warn("asr task occurred exception: " + e.getMessage());
        }

        log.debug("imageService.handleMultiFiles: " + folderPath);
        ParseResult<OCR> ocrResult = imageService.handle(folderPath, paramMap);

        ResultCode code = ResultCode.SUCCESS;
        if (ocrResult.getCode().equals(ResultCode.OCR_FAILURE) || asrResult.getCode().equals(ResultCode.ASR_FAILURE)) {
            code = ResultCode.PART_PARSE_FAILURE;
        }

        // 是否需要视频缩略图
        boolean whetherNeedVideoPoster = MapUtils.getBoolean(paramMap, Constants.AI_WHETHER_NEED_VIDEO_POSTER, false);
        byte[] poster = new byte[0];
        if (whetherNeedVideoPoster) {
            try (
                    InputStream is = Files.newInputStream(Paths.get(folderPath, "00001.jpg"))
            ) {
                poster = new byte[is.available()];
                IOUtils.read(is, poster);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new ParseResult<>(code, new VAC(ocrResult.getResult(), asrResult.getResult(), poster));
    }
}
