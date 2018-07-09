package com.eastrobot.converter.service.impl;

import com.eastrobot.converter.model.FileType;
import com.eastrobot.converter.model.ParseResult;
import com.eastrobot.converter.service.ParserCallBack;
import com.eastrobot.converter.util.ChineseUtil;
import com.eastrobot.converter.util.ResourceUtil;
import com.eastrobot.converter.util.ffmpeg.FFmpegUtil;
import com.hankcs.hanlp.HanLP;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.eastrobot.converter.model.ResultCode.*;

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

    ParseResult handle(String audioFilePath, ParserCallBack callBack) {
        try {
            // 1. 是否切割文件 {segmentDuration} 每段 文件放入当前文件夹下当前文件名命名的文件夹下
            FFmpegUtil.splitSegFileToPcm(audioFilePath, segmentDuration);
        } catch (IOException e) {
            log.error("splitSegToPcm occurred exception, check the ffmpeg location is right.");
            return new ParseResult(ASR_FAILURE, e.getMessage(), "", "",null);
        }

        File[] allPcmFiles =  new File(ResourceUtil.getFolder(audioFilePath, ""))
                .listFiles(filename -> FileType.PCM.getExtension().equals(FilenameUtils.getExtension(filename.getName())));

        assert allPcmFiles != null;
        if (allPcmFiles.length > 1) {
            // 2. 遍历pcm文件 解析每段文本
            int corePoolSize = Runtime.getRuntime().availableProcessors() + 1;
            ExecutorService executor = Executors.newFixedThreadPool(corePoolSize);
            // 总任务数门阀
            final CountDownLatch latch = new CountDownLatch(allPcmFiles.length);
            // 存储音轨解析段-内容
            final ConcurrentHashMap<Integer, String> audioContentMap = new ConcurrentHashMap<>();
            AtomicBoolean hasOccurredException = new AtomicBoolean(false);
            // 存储音轨解析异常信息 [seg:message]
            StringBuffer exceptionBuffer = new StringBuffer();

            for (final File file : allPcmFiles) {
                final String filepath = file.getAbsolutePath();
                //提交音轨转文字任务
                executor.submit(() -> {
                    String baseName = FilenameUtils.getBaseName(filepath);
                    String currentSegIndex = StringUtils.substringAfterLast(baseName, "-");
                    try {
                        String content = callBack.doInParser(filepath);
                        log.debug("asrHandler parse {} result : {}", filepath, content);
                        audioContentMap.put(Integer.parseInt(currentSegIndex), content);
                    } catch (Exception e) {
                        log.warn("asrHandler parse seg audio occurred exception: {}", e.getMessage());
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
                log.error("asrHandler parse audio thread occurred exception : {}", e.getMessage());
            } finally {
                executor.shutdownNow();
            }

            // 2. 解析结束后 合并内容 提取关键字
            String resultText = ResourceUtil.map2SortByKeyAndMergeWithSplit(audioContentMap, "");
            if (StringUtils.isNotBlank(resultText)) {
                resultText = ChineseUtil.removeMessy(resultText);
                List<String> keywords = HanLP.extractKeyword(resultText, 100);
                String keyword = ResourceUtil.list2String(keywords, ",");
                if (hasOccurredException.get()) {
                    return new ParseResult(ASR_FAILURE, exceptionBuffer.toString(), keyword, resultText,null);
                } else {
                    return new ParseResult(SUCCESS, SUCCESS.getMsg(), keyword, resultText,null);
                }
            } else {
                return new ParseResult(PARSE_EMPTY, PARSE_EMPTY.getMsg(), "", "",null);
            }
        } else { // 音频只有一段 不分段直解处理
            try {
                String resultText = callBack.doInParser(allPcmFiles[0].getAbsolutePath());
                if (StringUtils.isNotBlank(resultText)) {
                    List<String> keywords = HanLP.extractKeyword(resultText, 100);
                    String keyword = ResourceUtil.list2String(keywords, ",");
                    return new ParseResult(SUCCESS, SUCCESS.getMsg(), keyword, resultText,null);
                }

                return new ParseResult(PARSE_EMPTY, PARSE_EMPTY.getMsg(), "", "",null);
            } catch (Exception e) {
                log.warn("asrHandler parse audio occurred exception: {}", e.getMessage());
                return new ParseResult(ASR_FAILURE, e.getMessage(), "", "",null);
            }
        }
    }
}
