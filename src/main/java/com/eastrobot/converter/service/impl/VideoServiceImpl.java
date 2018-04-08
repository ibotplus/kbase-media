package com.eastrobot.converter.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.eastrobot.converter.service.AudioService;
import com.eastrobot.converter.service.ImageService;
import com.eastrobot.converter.service.VideoService;
import com.eastrobot.converter.util.ResourceUtil;
import com.hankcs.hanlp.HanLP;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;

/**
 * VideoServiceImpl
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 10:18
 */
@Service
public class VideoServiceImpl implements VideoService {
    private static final Logger logger = LoggerFactory.getLogger(VideoServiceImpl.class);

    @Autowired
    private ImageService imageService;

    @Autowired
    private AudioService audioService;

    /**
     * <pre>
     *     视频分段抽取音轨(*.pcm),
     *     原始视频抽图片(see {@link com.eastrobot.converter.util.ffmpeg.FFmpeg}),遍历分段音轨解析文字,遍历图片解析文字提取关键字.
     *     图片识别调用腾讯Youtu-API (see {@link com.eastrobot.converter.service.YouTuService#ocr}),
     *     语音识别调用百度Speech-API (see {@link com.eastrobot.converter.util.BaiduSpeechUtils#asr})
     * </pre>
     *
     * @author Yogurt_lei
     * @date 2018-03-26 18:06
     */
    @Override
    public JSONObject parseVideo(final String videoPath) {
        boolean handleFlag = false;
        try {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            //提交视频分段抽取音轨任务 视频提取图片
            Future<Boolean> handleFuture = executor.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    audioService.runFfmpegParseAudiosCmd(videoPath);
                    imageService.runFfmpegParseImagesCmd(videoPath);

                    return true;
                }
            });

            try {
                handleFlag = handleFuture.get();
            } catch (Exception e) {
                logger.warn("parse video thread occured exception : [%s]", e.getMessage());
            } finally {
                executor.shutdownNow();
            }
        } catch (Exception e) {
            logger.warn("parse video [%s] failed ", videoPath);
            e.printStackTrace();
        }

        return this.doParseVideo(videoPath);
    }

    private JSONObject doParseVideo(final String videoPath) {
        // 排序生成的文件
        File dir = new File(ResourceUtil.getFolder(videoPath, ""));
        List<File> allFiles = Arrays.asList(dir.listFiles());
        allFiles.sort((o1, o2) -> {
            if (o1.isDirectory() && o2.isFile())
                return -1;
            if (o1.isFile() && o2.isDirectory())
                return 1;
            return o1.getName().compareTo(o2.getName());
        });

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        // 总任务数门阀
        final CountDownLatch latch = new CountDownLatch(allFiles.size());
        // 存储音轨解析段-内容
        final ConcurrentHashMap<String, String> audioContentMap = new ConcurrentHashMap<String, String>();
        // 存储图片解析段-内容
        final ConcurrentHashMap<String, String> imageContentMap = new ConcurrentHashMap<String, String>();

        for (final File file : allFiles) {
            final String filepath = file.getAbsolutePath();
            if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("pcm")) {
                //提交音轨转文字任务
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String content = audioService.handle(filepath);
                            logger.debug("audioService parse [%s] result : [%s]", filepath, content);
                            audioContentMap.put(FilenameUtils.getBaseName(filepath), content);
                            // 语音文件转写完删除
                            FileUtils.deleteQuietly(file);
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }
            if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("jpg")) {
                //提交图片转文字任务
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // String content = imageService.handle(filepath);
                            String content = "";
                            logger.debug("imageService parse [%s] result : [%s]", filepath, content);
                            imageContentMap.put(FilenameUtils.getBaseName(filepath), content);
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }
        }

        try {
            // 阻塞等待结束
            latch.await();
        } catch (Exception e) {
            logger.warn("handle parse image or video thread occured exception : [%s]", e.getMessage());
        } finally {
            executor.shutdownNow();
        }

        String imgContentResult = null;
        // 如果是 分段提取关键字
        if (true) {
            StringBuilder segmentContent = new StringBuilder();
            //分段提取
            TreeMap<String, String> treeMap = ResourceUtil.map2SortByKey(imageContentMap);
            for (Map.Entry<String, String> entry : treeMap.entrySet()) {
                segmentContent.append(ResourceUtil.list2String(HanLP.extractKeyword(entry.getValue(), 10), "")).append(",");
            }
            imgContentResult = segmentContent.toString();
        } else {
            String imageContent = ResourceUtil.map2SortStringByKey(imageContentMap, "");
            //整文提取
            List<String> phraseList = HanLP.extractKeyword(imageContent, 200);
            imgContentResult = ResourceUtil.list2String(phraseList, "");
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("audios", ResourceUtil.map2SortStringByKey(audioContentMap, ""));
        jsonObject.put("images", imgContentResult);

        logger.info("parse video result Json: [%s]", jsonObject);

        return jsonObject;
    }
}
