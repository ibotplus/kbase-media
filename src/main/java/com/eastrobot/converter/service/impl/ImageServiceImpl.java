package com.eastrobot.converter.service.impl;

import com.eastrobot.converter.model.Constants;
import com.eastrobot.converter.model.ParseResult;
import com.eastrobot.converter.service.ImageService;
import com.eastrobot.converter.util.ResourceUtil;
import com.eastrobot.converter.util.abbyy.AbbyyOcrUtil;
import com.eastrobot.converter.util.youtu.YouTuOcrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
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

    @Value("${convert.image.ocr.default}")
    private String imageTool;

    @Override
    public ParseResult handle(String imageFilePath) {
        String result;
        try {
            if (Constants.YOUTU.equals(imageTool)) {
                result = YouTuOcrUtil.ocr(imageFilePath);
            } else if (Constants.ABBYY.equals(imageTool)) {
                result = AbbyyOcrUtil.ocr(imageFilePath);
            } else {
                return new ParseResult(CFG_ERROR, "", "");
            }
        } catch (Exception e) {
            log.warn("handler parse image occurred exception: {}", e.getMessage());
            return new ParseResult(OCR_FAILURE, e.getMessage(), "");
        }

        if (StringUtils.isNotBlank(result)) {
            return new ParseResult(SUCCESS, "", result);
        } else {
            return new ParseResult(PARSE_EMPTY, "", "");
        }
    }

    @Override
    public ParseResult handle(File... imageFiles) {
        int corePoolSize = Runtime.getRuntime().availableProcessors() + 1;
        ExecutorService executor = Executors.newFixedThreadPool(corePoolSize);
        // 总任务数门阀
        final CountDownLatch latch = new CountDownLatch(imageFiles.length);
        // 存储图片解析段-内容
        final ConcurrentHashMap<Integer, String> imageContentMap = new ConcurrentHashMap<>();
        AtomicBoolean hasOccurredException = new AtomicBoolean(false);
        // 存储图片解析异常信息 [seg:message]
        StringBuffer exceptionBuffer = new StringBuffer();

        for (File file : imageFiles) {
            String filePath = file.getAbsolutePath();
            //提交图片转文字任务
            executor.submit(() -> {
                String currentSegIndex = FilenameUtils.getBaseName(filePath);
                try {
                    ParseResult parseResult = handle(filePath);
                    if (parseResult.getCode().equals(SUCCESS)) {
                        String content = parseResult.getResult();
                        log.debug("imageHandler parse {} result : {}", filePath, content);
                        imageContentMap.put(Integer.parseInt(currentSegIndex), content);
                    } else {
                        throw new Exception(parseResult.getMessage());
                    }
                } catch (Exception e) {
                    log.warn("convert image occurred exception: {}", e.getMessage());
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
            log.error("imageHandler parse image thread occurred exception : {}", e.getMessage());
        } finally {
            executor.shutdownNow();
        }

        // 2. 解析结束后 合并内容
        String resultText = ResourceUtil.map2SortByKey(imageContentMap, "");
        if (StringUtils.isNotBlank(resultText)) {
            if (hasOccurredException.get()) {
                return new ParseResult(OCR_PART_PARSE_FAILED, exceptionBuffer.toString(), resultText);
            } else {
                return new ParseResult(SUCCESS, "", resultText);
            }
        } else {
            return new ParseResult(PARSE_EMPTY, "", "");
        }
    }
}
