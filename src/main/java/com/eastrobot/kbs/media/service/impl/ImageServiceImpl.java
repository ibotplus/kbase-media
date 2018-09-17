package com.eastrobot.kbs.media.service.impl;

import com.eastrobot.kbs.media.model.Constants;
import com.eastrobot.kbs.media.model.FileExtensionType;
import com.eastrobot.kbs.media.model.ParseResult;
import com.eastrobot.kbs.media.model.ResultCode;
import com.eastrobot.kbs.media.model.aitype.OCR;
import com.eastrobot.kbs.media.service.ImageService;
import com.eastrobot.kbs.media.util.ChineseUtil;
import com.eastrobot.kbs.media.util.ResourceUtil;
import com.eastrobot.kbs.media.util.abbyy.AbbyyOcrUtil;
import com.eastrobot.kbs.media.util.concurrent.ExecutorType;
import com.eastrobot.kbs.media.util.concurrent.ThreadPoolUtil;
import com.eastrobot.kbs.media.util.tesseract.TesseractUtil;
import com.eastrobot.kbs.media.util.youtu.YouTuOcrUtil;
import com.hankcs.hanlp.HanLP;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

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
    public ParseResult<OCR> handle(String imageFilePath) {
        String result;
        try {
            if (Constants.YOUTU.equals(imageTool)) {
                result = YouTuOcrUtil.ocr(imageFilePath);
            } else if (Constants.ABBYY.equals(imageTool)) {
                result = AbbyyOcrUtil.ocr(imageFilePath);
            } else if (Constants.TESSERACT.equals(imageTool)) {
                result = TesseractUtil.ocr(imageFilePath);
            } else {
                return new ParseResult<>(ResultCode.CFG_ERROR, null);
            }
        } catch (Exception e) {
            log.warn("handler parse image occurred exception: {}", e.getMessage());
            return new ParseResult<>(ResultCode.OCR_FAILURE, null);
        }

        if (StringUtils.isNotBlank(result)) {
            result = ChineseUtil.removeMessy(result);
            List<String> keywords = HanLP.extractKeyword(result, 100);
            String keyword = ResourceUtil.list2String(keywords, "");

            return new ParseResult<>(ResultCode.SUCCESS, new OCR(result, keyword));
        } else {
            return new ParseResult<>(ResultCode.PARSE_EMPTY, null);
        }
    }

    @Override
    public ParseResult<OCR> handleMultiFiles(String folder, boolean eachPictureExtractKeyword) {
        //视频帧图片
        File[] allImageFiles = Optional.of(new File(folder))
                .map(f -> f.listFiles(pathname ->
                        FilenameUtils.getExtension(pathname.getName()).equals(FileExtensionType.JPG.ext())))
                .orElseGet(() -> new File[0]);
        int imageCount = allImageFiles.length;
        // 总任务数门阀
        final CountDownLatch latch = new CountDownLatch(imageCount);
        // 存储图片解析段-内容
        Map<Integer, String> imageContentMap = new ConcurrentHashMap<>(imageCount);
        // 存储图片解析段-关键字
        Map<Integer, String> imageKeywordMap = new ConcurrentHashMap<>(imageCount);
        // 是否有异常产生标示
        AtomicBoolean hasOccurredException = new AtomicBoolean(false);
        // 存储图片解析异常信息 [seg:message]
        StringBuffer exceptionBuffer = new StringBuffer();
        ExecutorService executor = ThreadPoolUtil.ofExecutor(ExecutorType.GENERIC_IO_INTENSIVE);
        //提交图片转文字任务
        for (final File file : allImageFiles) {
            final String filepath = file.getAbsolutePath();
            //提交图片转文字任务
            executor.submit(() -> {
                // 图片的段是 00001 00002 00003
                String currentSegIndex = FilenameUtils.getBaseName(filepath);
                try {
                    ParseResult<OCR> parseResult = handle(filepath);
                    if (parseResult.getCode().equals(ResultCode.SUCCESS)) {
                        String keyword = parseResult.getResult().getImageKeyword();
                        String content = parseResult.getResult().getImageContent();
                        log.debug("imageService convert {} result : {}", filepath, content);
                        imageKeywordMap.put(Integer.parseInt(currentSegIndex), keyword);
                        imageContentMap.put(Integer.parseInt(currentSegIndex), content);
                    } else {
                        throw new Exception(parseResult.getCode().getMsg());
                    }
                } catch (Exception e) {
                    log.debug("convert image occurred exception: {}", e.getMessage());
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
            log.warn("imageService convert image thread occurred exception.", e);
        }

        // ocr 解析结果
        ParseResult<OCR> ocrResult;
        // 2. 解析结束后 合并内容 根据frameExtractKeyword 绝定是否需要帧抽取关键字
        String imageKeyword = ResourceUtil.map2SortByKeyAndMergeWithSplit(imageKeywordMap, ",");
        String imageContent = ResourceUtil.map2SortByKeyAndMergeWithSplit(imageContentMap, "");

        if (StringUtils.isNotBlank(imageContent)) {
            if (!eachPictureExtractKeyword) {
                List<String> phraseList = HanLP.extractKeyword(imageContent, 100);
                imageKeyword = ResourceUtil.list2String(phraseList, "");
            }

            if (hasOccurredException.get()) {
                ocrResult = new ParseResult<>(ResultCode.OCR_FAILURE, new OCR(imageContent, imageKeyword));
                log.warn("video handle occurred exception:{}", exceptionBuffer.toString());
            } else {
                ocrResult = new ParseResult<>(ResultCode.SUCCESS, new OCR(imageKeyword, imageContent));
            }
        } else {
            ocrResult = new ParseResult<>(ResultCode.PARSE_EMPTY, null);
        }

        return ocrResult;
    }
}
