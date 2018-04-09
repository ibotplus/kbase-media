package com.eastrobot.converter.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.eastrobot.converter.model.*;
import com.eastrobot.converter.service.AudioService;
import com.eastrobot.converter.service.ConvertService;
import com.eastrobot.converter.service.ImageService;
import com.eastrobot.converter.service.VideoService;
import com.eastrobot.converter.util.ResourceUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;

/**
 * MultiMediaConverterServiceImpl
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-29 14:39
 */
@Service
public class ConvertServiceImpl implements ConvertService {

    @Value("${convert.outputFolder}")
    private String OUTPUT_FOLDER;

    @Autowired
    private VideoService videoService;

    @Autowired
    private AudioService audioService;

    @Autowired
    private ImageService imageService;

    @Override
    public String getDefaultOutputFolderPath(String sn) {
        return OUTPUT_FOLDER + File.separator + DateFormatUtils.format(new Date(), "yyyyMMdd") + File.separator + sn;
    }

    @Override
    public ResponseMessage driver(String resPath) {
        JSONObject json = new JSONObject();
        File resFile = new File(resPath);
        ResponseMessage responseMessage = new ResponseMessage();

        if (ResourceUtil.isAudio(resPath)) {
            AsrParseResult handle = audioService.handle(resPath);

        } else if (ResourceUtil.isVideo(resPath)) {
            JSONObject videoJSON = videoService.parseVideo(resPath);
            String audioContent = videoJSON.getString(Constants.AUDIO_CONTENT);
            String imageKeyWord = videoJSON.getString(Constants.IMAGE_KEYWORD);
            String imageContent = videoJSON.getString(Constants.IMAGE_CONTENT);

            json.put("flag", "success");
            json.put("file_type", Constants.VIDEO);
            if (StringUtils.isNotBlank(audioContent)) {
                json.put("content", audioContent);
            }
            if (StringUtils.isNotBlank(imageKeyWord)) {
                json.put("keyword", imageKeyWord);
            }
            if (StringUtils.isNotBlank(imageContent)) {
                json.put("imageContent", imageContent);
            }
            if (StringUtils.isBlank(audioContent) && StringUtils.isBlank(imageKeyWord)) {
                json.put("flag", "failed");
                json.put("err_code", "1001");
                json.put("err_msg", "empty result");
            }
        } else if (ResourceUtil.isImage(resPath)) {
            OcrParseResult ocrResult = imageService.handle(resPath);

            if (ocrResult.getCode() == ResultCode.SUCCESS.getCode()) {
                ResponseEntity entity = new ResponseEntity();
                entity.setFileType(Constants.IMAGE);
                entity.setImageContent(ocrResult.getResult());
                responseMessage.setResponseEntity(entity);
            } else {
                if (ocrResult.getCode() == ResultCode.PARSE_EMPTY.getCode()) {
                    responseMessage.setResultCode(ResultCode.PARSE_EMPTY);
                } else {
                    responseMessage.setResultCode(ResultCode.OCR_FAILURE);
                    responseMessage.setMessage(ocrResult.getResult());
                }
            }
        } else {
            responseMessage.setResultCode(ResultCode.ILLEGAL_TYPE);
        }

        return responseMessage;
    }
}
