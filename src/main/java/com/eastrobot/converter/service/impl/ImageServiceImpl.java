package com.eastrobot.converter.service.impl;

import com.eastrobot.converter.model.Constants;
import com.eastrobot.converter.model.ParseResult;
import com.eastrobot.converter.service.ImageService;
import com.eastrobot.converter.service.YouTuService;
import com.eastrobot.converter.util.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.eastrobot.converter.model.ResultCode.*;

/**
 * ImageServiceImpl
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 18:54
 */
@Slf4j
@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private YouTuService youTuService;

    @Value("${convert.image.ocr.default}")
    private String imageTool;

    @Override
    public ParseResult handle(String... imageFilePaths) {
        if (Constants.YOUTU.equals(imageTool)) {
            return this.imageHandler(imageFilePaths);
        } else {
            return new ParseResult(CFG_ERROR, "","");
        }
    }

    private ParseResult imageHandler(String... imageFilePaths) {
        if (imageFilePaths.length > 1) {
            int corePoolSize = Runtime.getRuntime().availableProcessors() + 1;
            ExecutorService executor = Executors.newFixedThreadPool(corePoolSize);
            // 总任务数门阀
            final CountDownLatch latch = new CountDownLatch(imageFilePaths.length);
            // 存储图片解析段-内容
            final ConcurrentHashMap<Integer, String> imageContentMap = new ConcurrentHashMap<>();
            AtomicBoolean hasOccurredException = new AtomicBoolean(false);
            // 存储图片解析异常信息 [seg:message]
            StringBuffer exceptionBuffer = new StringBuffer();

            for (String imageFile : imageFilePaths) {
                //提交图片转文字任务
                executor.submit(() -> {
                    String currentSegIndex = FilenameUtils.getBaseName(imageFile);
                    try {
                        String content = doYoutuHandler(imageFile);
                        log.debug("youtuHandler parse {} result : {}", imageFilePaths, content);
                        Optional.ofNullable(content).ifPresent((Value) -> {
                            if (StringUtils.isNotBlank(Value)) {
                                imageContentMap.put(Integer.parseInt(currentSegIndex), Value);
                            }
                        });
                    } catch (Exception e) {
                        log.warn("youtuHandler parse seg image occurred exception: {}", e.getMessage());
                        hasOccurredException.set(true);
                        exceptionBuffer.append("[").append(currentSegIndex).append(":").append(e.getMessage()).append
                                ("]");
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
                log.error("youtuHandler parse image thread occurred exception : {}", e.getMessage());
            } finally {
                executor.shutdownNow();
            }

            // 2. 解析结束后 合并内容
            if (hasOccurredException.get()) {
                return new ParseResult(OCR_PART_PARSE_FAILED, exceptionBuffer.toString(), ResourceUtil.map2SortByKey(imageContentMap, ""));
            } else {
                return new ParseResult(SUCCESS, "", ResourceUtil.map2SortByKey(imageContentMap, ""));
            }
        } else {// 一张图片
            try {
                String result = this.doYoutuHandler(imageFilePaths[0]);
                return new ParseResult(SUCCESS, "", result);
            } catch (Exception e) {
                log.warn("handler parse image occurred exception: {}", e.getMessage());
                return new ParseResult(OCR_FAILURE, e.getMessage(), "");
            }
        }
    }

    private String doYoutuHandler(String imageFilePath) throws Exception {
        return youTuService.ocr(imageFilePath);
    }
}
