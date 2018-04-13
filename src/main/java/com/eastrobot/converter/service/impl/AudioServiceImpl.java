package com.eastrobot.converter.service.impl;

import com.eastrobot.converter.model.AsrParseResult;
import com.eastrobot.converter.model.Constants;
import com.eastrobot.converter.model.FFmpegFileType;
import com.eastrobot.converter.service.AudioService;
import com.eastrobot.converter.util.ResourceUtil;
import com.eastrobot.converter.util.baidu.BaiduSpeechUtils;
import com.eastrobot.converter.util.ffmpeg.FFmpegUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.eastrobot.converter.model.ResultCode.*;
import static com.eastrobot.converter.util.baidu.BaiduAsrConstants.*;


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
    public AsrParseResult handle(String audioFilePath) {
        if (Constants.BAIDU.equals(audioTool)) {
           return this.baiduAsrHandler(audioFilePath);
        } else {
            return new AsrParseResult(CFG_ERROR, "","");
        }
    }

    /**
     * @author Yogurt_lei
     * @date 2018-04-10 10:21
     */
    private AsrParseResult baiduAsrHandler(String audioFilePath) {
        double duration = 0;
        try {
            duration = FFmpegUtil.getDuration(audioFilePath);
        } catch (IOException e) {
            log.error("getDuration occurred exception, check the ffmpeg location is right.");
            return new AsrParseResult(FFMPEG_LOCATION_ERROR, "", "");
        }
        if (duration > MAX_DURATION) {
            // 1. 切割文件 59s 每段
            String folder = ResourceUtil.getFolder(audioFilePath, "");
            int totalSegment = (int) (duration / MAX_DURATION + ((duration % MAX_DURATION) > 0 ? 1 : 0));
            log.debug("total segment :[%s], total second: [%s]", totalSegment, duration);
            for (int i = 1; i <= totalSegment; i++) {
                FFmpegUtil.splitBaiduAsrAudio(audioFilePath, (i - 1) * MAX_DURATION,
                        folder + FilenameUtils.getBaseName(audioFilePath) + "-" + i + ".pcm");
            }

            // 2. 遍历pcm文件 解析每段文本
            File dir = new File(folder);
            File[] allPcmFiles = dir.listFiles(filename -> "pcm".equals(FilenameUtils.getExtension(filename.getName())));

            int corePoolSize = Runtime.getRuntime().availableProcessors() + 1;
            ExecutorService executor = Executors.newFixedThreadPool(corePoolSize);
            // 总任务数门阀
            final CountDownLatch latch = new CountDownLatch(allPcmFiles.length);
            // 存储音轨解析段-内容
            final ConcurrentHashMap<Integer, String> audioContentMap = new ConcurrentHashMap<>();
            AtomicBoolean hasOccuredException = new AtomicBoolean(false);
            // 存储音轨解析异常信息 [seg:message]
            StringBuffer exceptionBuffer = new StringBuffer();

            for (final File file : allPcmFiles) {
                final String filepath = file.getAbsolutePath();
                //提交音轨转文字任务
                executor.submit(()->{
                    String baseName = FilenameUtils.getBaseName(filepath);
                    String currentSegIndex = StringUtils.substringAfterLast(baseName, "-");
                    try {
                        String content = doBaiduAsrHandler(filepath);
                        log.debug("baiduHandler parse {} result : {}", filepath, content);
                        Optional.ofNullable(content).ifPresent((Value) -> {
                            if (StringUtils.isNotBlank(Value)) {
                                audioContentMap.put(Integer.parseInt(currentSegIndex), Value);
                            }
                        });
                    } catch (Exception e) {
                        log.warn("baiduHandler parse seg audio occured exception: {}", e.getMessage());
                        hasOccuredException.set(true);
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
                hasOccuredException.set(true);
                exceptionBuffer.append("[").append(e.getMessage()).append("]");
                log.error("baiduHandler parse audio thread occured exception : {}", e.getMessage());
            } finally {
                // 语音文件转写完删除 测试不打开
                // FileUtils.deleteQuietly(dir);
                executor.shutdownNow();
            }

            // 2. 解析结束后 合并内容
            if (hasOccuredException.get()) {
                return new AsrParseResult(ASR_PART_PARSE_FAILED, exceptionBuffer.toString(), ResourceUtil.map2SortByKey(audioContentMap, ""));
            } else {
                return new AsrParseResult(SUCCESS, "", ResourceUtil.map2SortByKey(audioContentMap, ""));
            }
        } else { // 音频长度小于60 不分段直解处理
            try {
                String text = this.doBaiduAsrHandler(audioFilePath);
                return new AsrParseResult(SUCCESS, "", text);
            } catch (Exception e) {
                log.warn("baiduHandler parse seg audio occured exception: {}", e.getMessage());
                return new AsrParseResult(ASR_PART_PARSE_FAILED, e.getMessage(), "");
            }
        }
    }

    private String doBaiduAsrHandler(String audioFilePath) throws Exception {
        String pcmAudioFile = audioFilePath;
        // 不是pcm格式转成pcm格式
        if (!"pcm".equalsIgnoreCase(FilenameUtils.getExtension(audioFilePath))) {
            pcmAudioFile = FFmpegUtil.transformAudio(audioFilePath, FFmpegFileType.PCM);
            FileUtils.deleteQuietly(new File(audioFilePath));
        }
        JSONObject asr = BaiduSpeechUtils.asr(pcmAudioFile, PCM, RATE);
        if (asr.optInt("err_no", -1) == 0) {
            //数组字符串
            String result = asr.optString("result");
            result = StringUtils.substringBetween(result, "[\"", "\"]");

            return result;
        } else {
            throw new Exception(asr.getString("err_msg"));
        }
    }
}
