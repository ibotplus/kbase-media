package com.eastrobot.converter.service.impl;

import com.eastrobot.converter.config.ConvertConfig;
import com.eastrobot.converter.model.AsrParseResult;
import com.eastrobot.converter.model.Constants;
import com.eastrobot.converter.model.ResultCode;
import com.eastrobot.converter.service.AudioService;
import com.eastrobot.converter.util.ResourceUtil;
import com.eastrobot.converter.util.baidu.BaiduAsrConstants;
import com.eastrobot.converter.util.baidu.BaiduSpeechUtils;
import com.eastrobot.converter.util.ffmpeg.FFmpegUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * AudioServiceImpl
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 18:54
 */
@Slf4j
@Service
public class AudioServiceImpl implements AudioService {

    @Autowired
    private ConvertConfig convertConfig;

    @Override
    public AsrParseResult handle(String audioFilePath) {
        String audioTool = (String) convertConfig.getDefaultAudioConfig().get(Constants.DEFAULT_TOOL);

        if (Constants.BAIDU.equals(audioTool)) {
            String result = this.baiduHandler(audioFilePath);
        } else {
            return new AsrParseResult(ResultCode.CFG_ERROR.getCode(), "");
        }

        return null;
    }

    public String baiduHandler(String audioFilePath) {
        double duration = FFmpegUtil.getDuration(audioFilePath);
        if (duration > BaiduAsrConstants.MAX_DURATION) {
            // 1. 切割文件 59s 每段
            String folder = ResourceUtil.getFolder(audioFilePath, "");
            int totalSegment = (int) (duration / BaiduAsrConstants.MAX_DURATION + ((duration % BaiduAsrConstants.MAX_DURATION) > 0 ? 1 : 0));
            log.debug("total segment :[%s], total second: [%s]", totalSegment, duration);
            for (int i = 1; i <= totalSegment; i++) {
                FFmpegUtil.splitBaiduAsrAudio(audioFilePath,(i - 1) * BaiduAsrConstants.MAX_DURATION,
                        folder + FilenameUtils.getBaseName(audioFilePath) + "-" + i + ".pcm");
            }

            // 排序生成的文件
            File dir = new File(ResourceUtil.getFolder(audioFilePath, ""));
            List<File> allFiles = Arrays.asList(Objects.requireNonNull(dir.listFiles()));

            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
            // 总任务数门阀
            final CountDownLatch latch = new CountDownLatch(allFiles.size());
            // 存储音轨解析段-内容
            final ConcurrentHashMap<Integer, String> audioContentMap = new ConcurrentHashMap<>();

            for (final File file : allFiles) {
                final String filepath = file.getAbsolutePath();
                //提交音轨转文字任务
                executor.submit(()->{
                    try {
                        String content = doBaiduHandler(filepath);
                        log.debug("audioService parse [%s] result : [%s]", filepath, content);
                        Optional.ofNullable(content).ifPresent((Value)->{
                            if (StringUtils.isNotBlank(Value)) {
                                String baseName = FilenameUtils.getBaseName(filepath);
                                String index = StringUtils.substringAfterLast(baseName, "-");
                                audioContentMap.put(Integer.parseInt(index), Value);
                            }
                        });

                        // 语音文件转写完删除
                        FileUtils.deleteQuietly(file);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            try {
                // 阻塞等待结束
                latch.await();
            } catch (Exception e) {
                log.warn("handle parse image or video thread occured exception : [%s]", e.getMessage());
            } finally {
                FileUtils.deleteQuietly(dir);
                executor.shutdownNow();
            }

            //FIXME 此处排序有问题 待改动
            return ResourceUtil.map2SortStringByKey(audioContentMap, "");
        } else { // 音频长度小于60 不分段直解处理
            return this.doBaiduHandler(audioFilePath);
        }
    }


    private String doBaiduHandler(String audioFilePath) {
        JSONObject asr = BaiduSpeechUtils.asr(audioFilePath, BaiduAsrConstants.PCM, BaiduAsrConstants.RATE);
        if (asr.optInt("err_no", -1) == 0) {
            //数组字符串
            String result = asr.optString("result");
            result = StringUtils.substringBetween(result, "[\"", "\"]");

            return result;
        }
        // TODO by Yogurt_lei :  百度 asr 错误码

        return null;
    }
}
