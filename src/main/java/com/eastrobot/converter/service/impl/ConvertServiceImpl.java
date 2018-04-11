package com.eastrobot.converter.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.eastrobot.converter.model.*;
import com.eastrobot.converter.service.AudioService;
import com.eastrobot.converter.service.ConvertService;
import com.eastrobot.converter.service.ImageService;
import com.eastrobot.converter.service.VideoService;
import com.eastrobot.converter.util.ResourceUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;

import static com.eastrobot.converter.model.ResultCode.*;

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

    // 文件路径:${convert.outputFolder}/yyyyMMDD/sn.extension
    public String doUpload(MultipartFile file, String sn) throws Exception {
        String targetFile = OUTPUT_FOLDER + File.separator
                + DateFormatUtils.format(new Date(), "yyyyMMdd") + File.separator
                + sn + FilenameUtils.getExtension(file.getOriginalFilename());
        File tmpFile = new File(targetFile);
        tmpFile.mkdirs();
        file.transferTo(tmpFile);

        return targetFile;
    }

    @Override
    public ResponseMessage driver(String resPath) {
        JSONObject json = new JSONObject();
        File resFile = new File(resPath);
        ResponseMessage responseMessage = new ResponseMessage();

        if (ResourceUtil.isAudio(resPath)) {
            AsrParseResult asrResult = audioService.handle(resPath);

            if (SUCCESS.equals(asrResult.getCode())) {
                ResponseEntity entity = new ResponseEntity();
                entity.setFileType(Constants.AUDIO);
                entity.setAudioContent(asrResult.getResult());
                responseMessage.setResponseEntity(entity);
            } else if (BAIDU_ASR_SEG_FAILURE.equals(asrResult.getCode())) {
                if (StringUtils.isBlank(asrResult.getResult())) {
                    responseMessage.setResultCode(PARSE_EMPTY);
                    responseMessage.setMessage(asrResult.getMessage());
                } else {
                    responseMessage.setResultCode(BAIDU_ASR_SEG_FAILURE);
                    ResponseEntity entity = new ResponseEntity();
                    entity.setFileType(Constants.AUDIO);
                    entity.setAudioContent(asrResult.getResult());
                    responseMessage.setResponseEntity(entity);
                }
            }
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

            if (SUCCESS.equals(ocrResult.getCode())) {
                ResponseEntity entity = new ResponseEntity();
                entity.setFileType(Constants.IMAGE);
                entity.setImageContent(ocrResult.getResult());
                responseMessage.setResponseEntity(entity);
            } else {
                if (ocrResult.getCode().equals(PARSE_EMPTY)) {
                    responseMessage.setResultCode(PARSE_EMPTY);
                } else {
                    responseMessage.setResultCode(OCR_FAILURE);
                    responseMessage.setMessage(ocrResult.getResult());
                }
            }
        } else {
            responseMessage.setResultCode(ILLEGAL_TYPE);
        }

        return responseMessage;
    }
}
