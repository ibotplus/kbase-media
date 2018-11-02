package com.eastrobot.kbs.media.service.impl;

import com.eastrobot.kbs.media.model.FileExtensionType;
import com.eastrobot.kbs.media.model.ParseResult;
import com.eastrobot.kbs.media.model.ResultCode;
import com.eastrobot.kbs.media.model.aitype.ASR;
import com.eastrobot.kbs.media.service.ParserCallBack;
import com.eastrobot.kbs.media.util.ChineseUtil;
import com.eastrobot.kbs.media.util.ResourceUtil;
import com.eastrobot.kbs.media.util.concurrent.ExecutorType;
import com.eastrobot.kbs.media.util.concurrent.ThreadPoolUtil;
import com.eastrobot.kbs.media.util.ffmpeg.FFmpegUtil;
import com.hankcs.hanlp.HanLP;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AudioParserTemplate
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-16 14:46
 */
@Slf4j
@Component
public class AudioParserTemplate {

    @Value("${convert.audio.asr.seg-duration}")
    private Long segmentDuration;

    ParseResult<ASR> handle(String audioFilePath, ParserCallBack callBack) {
        try {
            // 1. 是否切割文件 {segmentDuration} 每段,文件放入当前文件夹下 filename-%d.pcm
            FFmpegUtil.splitSegFileToPcm(audioFilePath, segmentDuration);
        } catch (IOException e) {
            log.error("splitSegToPcm occurred exception, check the ffmpeg location is right.");
            return new ParseResult<>(ResultCode.ASR_FAILURE, null);
        }

        // 得到所有待解析pcm
        File[] allPcmFiles = Paths.get(FilenameUtils.getFullPath(audioFilePath))
                .toFile()
                .listFiles(filename -> filename.getName().endsWith(FileExtensionType.PCM.ext()));

        if (Objects.requireNonNull(allPcmFiles).length > 1) {
            return multiSegAudioFileParse(allPcmFiles, callBack);
        } else {
            try {
                String resultText = callBack.doInParser(allPcmFiles[0].getAbsolutePath());
                if (StringUtils.isNotBlank(resultText)) {
                    List<String> keywords = HanLP.extractKeyword(resultText, 100);
                    String keyword = ResourceUtil.list2String(keywords, ",");
                    return new ParseResult<>(ResultCode.SUCCESS, new ASR(resultText, keyword));
                }

                return new ParseResult<>(ResultCode.PARSE_EMPTY, null);
            } catch (Exception e) {
                log.warn("asrHandler parse audio occurred exception: {}", e.getMessage());
                return new ParseResult<>(ResultCode.ASR_FAILURE, null);
            }
        }
    }

    /**
     * 解析多段音频的文件
     */
    private ParseResult<ASR> multiSegAudioFileParse(File[] allPcmFiles, ParserCallBack callBack) {
        // 2. 遍历pcm文件 解析每段文本
        ExecutorService executor = ThreadPoolUtil.ofExecutor(ExecutorType.GENERIC_IO_INTENSIVE);
        int taskCount = allPcmFiles.length;
        // 总任务数门阀
        CountDownLatch latch = new CountDownLatch(taskCount);
        // 存储音轨解析段-内容
        ConcurrentHashMap<Integer, String> audioContentMap = new ConcurrentHashMap<>(taskCount);
        AtomicBoolean hasOccurredException = new AtomicBoolean(false);
        for (File file : allPcmFiles) {
            String filePath = file.getAbsolutePath();
            //提交音轨转文字任务
            log.debug("asrHandler executor submit asrTask {}", filePath);
            executor.submit(() -> {
                String baseName = FilenameUtils.getBaseName(filePath);
                String currentSegIndex = StringUtils.substringAfterLast(baseName, "-");
                try {
                    String content = callBack.doInParser(filePath);
                    log.debug("asrHandler parse {} result : {}", filePath, content);
                    audioContentMap.put(Integer.parseInt(currentSegIndex), content);
                } catch (Exception e) {
                    log.warn("asrHandler parse seg audio {} occurred exception: {}", baseName, e.getMessage());
                    hasOccurredException.set(true);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            // 阻塞等待结束
            latch.await(taskCount * 5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            hasOccurredException.set(true);
            log.error("asrHandler parse audio thread occurred exception : {}", e.getMessage());
        }

        // 2. 解析结束后 合并内容 提取关键字
        String resultText = ResourceUtil.map2SortByKeyAndMergeWithSplit(audioContentMap, "");
        if (StringUtils.isNotBlank(resultText)) {
            resultText = ChineseUtil.removeMessy(resultText);
            List<String> keywords = HanLP.extractKeyword(resultText, 100);
            String keyword = ResourceUtil.list2String(keywords, ",");
            if (hasOccurredException.get()) {
                return new ParseResult<>(ResultCode.ASR_FAILURE, new ASR(resultText, keyword));
            } else {
                return new ParseResult<>(ResultCode.SUCCESS, new ASR(resultText, keyword));
            }
        } else {
            return new ParseResult<>(ResultCode.PARSE_EMPTY, null);
        }
    }
}
