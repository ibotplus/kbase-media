package com.eastrobot.converter.service.impl;

import com.eastrobot.converter.service.AudioService;
import com.eastrobot.converter.util.BaiduSpeechUtils;
import com.eastrobot.converter.util.ResourceUtil;
import com.eastrobot.converter.util.ffmpeg.FFmpeg;
import com.eastrobot.converter.util.ffmpeg.FFmpegUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

    @Value("${convert.audio.asr.default}")
    private String ASR_TOOL;

    private static final String BAIDU = "baidu";

    @Override
    public Boolean runFfmpegParseAudiosCmd(final String videoPath) {
        String folder = ResourceUtil.getFolder(videoPath, "");

        StringBuffer resultBuffer = new StringBuffer();
        int totalSeconds = FFmpegUtil.getVideoTime(videoPath);
        int segmentSecond = 60;
        int totalSegment = totalSeconds / segmentSecond + ((totalSeconds % segmentSecond) > 0 ? 1 : 0);
        log.debug("total segment :[%s], total second: [%s]", totalSegment, totalSeconds);

        for (int i = 1; i <= totalSegment; i++) {
            FFmpeg fFmpeg = new FFmpeg("D:\\ffmpeg\\bin");
            fFmpeg.addParam("-y");
            fFmpeg.addParam("-i");
            fFmpeg.addParam(videoPath);
            fFmpeg.addParam("-ss");
            fFmpeg.addParam(FFmpegUtil.parseTimeToString((i - 1) * segmentSecond));
            fFmpeg.addParam("-t");
            fFmpeg.addParam("00:00:59");
            fFmpeg.addParam("-acodec");
            fFmpeg.addParam("pcm_s16le");
            fFmpeg.addParam("-f");
            fFmpeg.addParam("s16le");
            fFmpeg.addParam("-ac");
            fFmpeg.addParam("1");
            fFmpeg.addParam("-ar");
            fFmpeg.addParam("16000");
            fFmpeg.addParam(folder + FilenameUtils.getBaseName(videoPath) + "-" + i + ".pcm");
            try {
                fFmpeg.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } // for i end

        return true;
    }

    @Override
    public String handle(String audioFilePath) {
        //TODO 调用 NTE 接口实现语音提取文本
        if (BAIDU.equals(ASR_TOOL)) {
            JSONObject asr = BaiduSpeechUtils.asr(audioFilePath, "pcm", 16000);
            if (asr.optInt("err_no",-1) == 0) {
                //success
                //数组字符串
                String result = asr.optString("result");
                result = StringUtils.substringBetween(result, "[\"", "\"]");

                return result;
            }
        }
        return "";
    }

    public com.alibaba.fastjson.JSONObject parseAudio(String audioFilePath) {
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        this.runFfmpegParseAudiosCmd(audioFilePath);

        // 排序生成的文件
        File dir = new File(ResourceUtil.getFolder(audioFilePath, ""));
        List<File> allFiles = Arrays.asList(dir.listFiles());
        Collections.sort(allFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        // 总任务数门阀
        final CountDownLatch latch = new CountDownLatch(allFiles.size());
        // 存储音轨解析段-内容
        final ConcurrentHashMap<String, String> audioContentMap = new ConcurrentHashMap<String, String>();

        for (final File file : allFiles) {
            final String filepath = file.getAbsolutePath();
            //提交音轨转文字任务
            executor.submit(()->{
                try {
                    String content = handle(filepath);
                    log.debug("audioService parse [%s] result : [%s]", filepath, content);
                    audioContentMap.put(FilenameUtils.getBaseName(filepath), content);
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
            executor.shutdownNow();
        }

        jsonObject.put("audios", ResourceUtil.map2SortStringByKey(audioContentMap, ""));

        log.info("parse audio result Json: [%s]", jsonObject);

        return jsonObject;
    }
}
