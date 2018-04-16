package com.eastrobot.converter.service.impl;

import com.eastrobot.converter.model.Constants;
import com.eastrobot.converter.model.FileType;
import com.eastrobot.converter.model.ParseResult;
import com.eastrobot.converter.service.AudioService;
import com.eastrobot.converter.util.ResourceUtil;
import com.eastrobot.converter.util.baidu.BaiduSpeechUtils;
import com.eastrobot.converter.util.ffmpeg.FFmpegUtil;
import com.eastrobot.converter.util.shhan.ShhanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.eastrobot.converter.model.ResultCode.*;
import static com.eastrobot.converter.util.baidu.BaiduAsrConstants.PCM;
import static com.eastrobot.converter.util.baidu.BaiduAsrConstants.RATE;


/**
 * AudioServiceImpl
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 18:54
 */
@Slf4j
@Service
public class AudioServiceImpl implements AudioService {

    @Value("${convert.audio.asr.default}")
    private String audioTool;

    @Override
    public ParseResult handle(String audioFilePath) {
        if (Constants.BAIDU.equals(audioTool)) {
           return this.baiduAsrHandler(audioFilePath);
        } else if (Constants.SHHAN.equals(audioTool)) {
            return this.shhanAsrHandler(audioFilePath);
        } else {
            return new ParseResult(CFG_ERROR, "","");
        }
    }

    private ParseResult shhanAsrHandler(String audioFilePath) {
        try {
            // 1. 是否切割文件 20s 每段 文件放入当前文件夹下当前文件名命名的文件夹下
            FFmpegUtil.splitSegFileToPcm(audioFilePath, 20);
        } catch (IOException e) {
            log.error("splitSegToPcm occurred exception, check the ffmpeg location is right.");
            return new ParseResult(FFMPEG_LOCATION_ERROR, "", "");
        }
        File folder = new File(ResourceUtil.getFolder(audioFilePath, ""));
        File[] allPcmFiles = folder.listFiles(filename -> FileType.PCM.getExtension().equals(FilenameUtils.getExtension(filename.getName())));
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
                        String content = doShhanAsrHandler(filepath);
                        log.debug("shhanAsrHandler parse {} result : {}", filepath, content);
                        audioContentMap.put(Integer.parseInt(currentSegIndex), content);
                    } catch (Exception e) {
                        log.warn("shhanAsrHandler parse seg audio occurred exception: {}", e.getMessage());
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
                log.error("shhanAsrHandler parse audio thread occurred exception : {}", e.getMessage());
            } finally {
                executor.shutdownNow();
            }

            // 2. 解析结束后 合并内容
            if (hasOccurredException.get()) {
                return new ParseResult(ASR_PART_PARSE_FAILED, exceptionBuffer.toString(), ResourceUtil.map2SortByKey(audioContentMap, ""));
            } else {
                return new ParseResult(SUCCESS, "", ResourceUtil.map2SortByKey(audioContentMap, ""));
            }
        } else { // 音频长度小于20 不分段直解处理
            try {
                String text = this.doShhanAsrHandler(allPcmFiles[0].getAbsolutePath());
                return new ParseResult(SUCCESS, "", text);
            } catch (Exception e) {
                log.warn("doShhanAsrHandler parse audio occurred exception: {}", e.getMessage());
                return new ParseResult(ASR_FAILURE, e.getMessage(), "");
            }
        }
    }

    private String doShhanAsrHandler(String audioFilePath) throws Exception {
        String asr = ShhanUtil.asr(audioFilePath);
        if (StringUtils.isNotBlank(asr)) {
            return asr;
        } else {
            throw new Exception("empty result");
        }
    }

    /**
     * @author Yogurt_lei
     * @date 2018-04-10 10:21
     */
    private ParseResult baiduAsrHandler(String audioFilePath) {
        try {
            // 1. 是否切割文件 59s 每段 文件放入当前文件夹下当前文件名命名的文件夹下
            FFmpegUtil.splitSegFileToPcm(audioFilePath, 60);
        } catch (IOException e) {
            log.error("splitSegToPcm occurred exception, check the ffmpeg location is right.");
            return new ParseResult(FFMPEG_LOCATION_ERROR, "", "");
        }
        File folder = new File(ResourceUtil.getFolder(audioFilePath, ""));
        File[] allPcmFiles = folder.listFiles(filename -> FileType.PCM.getExtension().equals(FilenameUtils.getExtension(filename.getName())));
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
                        String content = doBaiduAsrHandler(filepath);
                        log.debug("baiduHandler parse {} result : {}", filepath, content);
                        audioContentMap.put(Integer.parseInt(currentSegIndex), content);
                    } catch (Exception e) {
                        log.warn("baiduHandler parse seg audio occurred exception: {}", e.getMessage());
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
                log.error("baiduHandler parse audio thread occurred exception : {}", e.getMessage());
            } finally {
                executor.shutdownNow();
            }

            // 2. 解析结束后 合并内容
            if (hasOccurredException.get()) {
                return new ParseResult(ASR_PART_PARSE_FAILED, exceptionBuffer.toString(), ResourceUtil.map2SortByKey(audioContentMap, ""));
            } else {
                return new ParseResult(SUCCESS, "", ResourceUtil.map2SortByKey(audioContentMap, ""));
            }
        } else { // 音频长度小于60 不分段直解处理
            try {
                String text = this.doBaiduAsrHandler(allPcmFiles[0].getAbsolutePath());
                return new ParseResult(SUCCESS, "", text);
            } catch (Exception e) {
                log.warn("doBaiduAsrHandler parse audio occurred exception: {}", e.getMessage());
                return new ParseResult(ASR_FAILURE, e.getMessage(), "");
            }
        }
    }

    private String doBaiduAsrHandler(String audioFilePath) throws Exception {
        JSONObject asr = BaiduSpeechUtils.asr(audioFilePath, PCM, RATE);
        if (asr.optInt("err_no", -1) == 0) {
            //数组字符串
            String result = asr.optString("result");
            result = StringUtils.substringBetween(result, "[\"", "\"]");

            if (StringUtils.isBlank(result)) {
                throw new Exception("empty result");
            }

            return result;
        } else {
            throw new Exception(asr.getString("err_msg"));
        }
    }
}
