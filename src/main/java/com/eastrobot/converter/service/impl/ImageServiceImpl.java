package com.eastrobot.converter.service.impl;

import com.eastrobot.converter.model.Constants;
import com.eastrobot.converter.model.ParseResult;
import com.eastrobot.converter.service.ImageService;
import com.eastrobot.converter.util.ChineseUtil;
import com.eastrobot.converter.util.ResourceUtil;
import com.eastrobot.converter.util.abbyy.AbbyyOcrUtil;
import com.eastrobot.converter.util.tesseract.TesseractUtil;
import com.eastrobot.converter.util.youtu.YouTuOcrUtil;
import com.hankcs.hanlp.HanLP;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

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
        ParseResult parseResult;
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
                    return new ParseResult(OCR_FAILURE, "undefined ocr tools", "", "",null);
            }
        } catch (Exception e) {
            log.warn("handler parse image occurred exception: {}", e.getMessage());
            return new ParseResult(OCR_FAILURE, e.getMessage(), "", "",null);
        }

        if (StringUtils.isNotBlank(result)) {
            result = ChineseUtil.removeMessy(result);
            List<String> keywords = HanLP.extractKeyword(result, 100);
            String keyword = ResourceUtil.list2String(keywords, "");

            return new ParseResult(SUCCESS, SUCCESS.getMsg(), keyword, result,null);
        } else {
            return new ParseResult(PARSE_EMPTY, PARSE_EMPTY.getMsg(), "", "",null);
        }
    }
}
