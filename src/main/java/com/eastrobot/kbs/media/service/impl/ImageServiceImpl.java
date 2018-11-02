package com.eastrobot.kbs.media.service.impl;

import com.eastrobot.kbs.media.exception.BusinessException;
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
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
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
    public ParseResult<OCR> handle(String imageFilePath, Map paramMap) {
        File originFile = Paths.get(imageFilePath).toFile();
        if (originFile.isFile()) {
            String result;
            try {
                switch (imageTool) {
                    case Constants.YOUTU:
                        result = YouTuOcrUtil.ocr(imageFilePath);
                        break;
                    case Constants.ABBYY:
                        result = AbbyyOcrUtil.ocr(imageFilePath);
                        break;
                    case Constants.TESSERACT:
                        result = TesseractUtil.ocr(imageFilePath);
                        break;
                    default:
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
        } else {
            return this.handleMultiFiles(imageFilePath, paramMap);
        }
    }

    private ParseResult<OCR> handleMultiFiles(String folder, Map paramMap) {
        //视频帧图片
        File[] allImageFiles = Paths.get(folder)
                .toFile()
                .listFiles(pathname -> pathname.getName().endsWith(FileExtensionType.JPG.ext()));
        ExecutorService executor = ThreadPoolUtil.ofExecutor(ExecutorType.GENERIC_IO_INTENSIVE);
        int taskCount = Objects.requireNonNull(allImageFiles).length;
        // 总任务数门阀
        final CountDownLatch latch = new CountDownLatch(taskCount);
        // 存储图片解析段-内容
        Map<Integer, String> imageContentMap = new ConcurrentHashMap<>(taskCount);
        // 存储图片解析段-关键字
        Map<Integer, String> imageKeywordMap = new ConcurrentHashMap<>(taskCount);
        // 是否有异常产生标示
        AtomicBoolean hasOccurredException = new AtomicBoolean(false);

        for (File file : allImageFiles) {
            final String filePath = file.getAbsolutePath();
            //提交图片转文字任务
            executor.submit(() -> {
                String baseName = FilenameUtils.getBaseName(filePath);
                try {
                    // 图片的段是 00001 00002 00003
                    Integer currentSegIndex = Integer.parseInt(baseName);
                    ParseResult<OCR> parseResult = handle(filePath, paramMap);
                    if (parseResult.getCode().equals(ResultCode.SUCCESS)) {
                        String keyword = parseResult.getResult().getImageKeyword();
                        String content = parseResult.getResult().getImageContent();
                        log.debug("imageService convert {} result : {}", filePath, content);
                        imageKeywordMap.put(currentSegIndex, keyword);
                        imageContentMap.put(currentSegIndex, content);
                    } else {
                        throw new BusinessException(parseResult.getCode().msg());
                    }
                } catch (Exception e) {
                    log.warn("handleMultiFiles parse image {} occurred exception: {}", baseName, e.getMessage());
                    hasOccurredException.set(true);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            // 阻塞等待结束
            latch.await(taskCount * 5, TimeUnit.SECONDS);
        } catch (Exception e) {
            hasOccurredException.set(true);
            log.warn("imageService convert image thread occurred exception.", e);
        }

        // ocr 解析结果
        ParseResult<OCR> ocrResult;
        // 2. 解析结束后 合并内容 根据frameExtractKeyword 决定是否需要帧抽取关键字
        String imageKeyword = ResourceUtil.map2SortByKeyAndMergeWithSplit(imageKeywordMap, ",");
        String imageContent = ResourceUtil.map2SortByKeyAndMergeWithSplit(imageContentMap, "");

        if (StringUtils.isNotBlank(imageContent)) {
            // 是否每个提取的图片都抽取关键字
            boolean eachPictureExtractKeyword = MapUtils.getBoolean(paramMap, Constants.AI_IS_FRAME_EXTRACT_KEYWORD, false);
            if (!eachPictureExtractKeyword) {
                List<String> phraseList = HanLP.extractKeyword(imageContent, 100);
                imageKeyword = ResourceUtil.list2String(phraseList, "");
            }

            if (hasOccurredException.get()) {
                ocrResult = new ParseResult<>(ResultCode.OCR_FAILURE, new OCR(imageContent, imageKeyword));
            } else {
                ocrResult = new ParseResult<>(ResultCode.SUCCESS, new OCR(imageKeyword, imageContent));
            }
        } else {
            ocrResult = new ParseResult<>(ResultCode.PARSE_EMPTY, null);
        }

        return ocrResult;
    }
}
