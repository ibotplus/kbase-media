package com.eastrobot.converter.web.controller;

import com.eastrobot.converter.exception.BusinessException;
import com.eastrobot.converter.model.ResponseMessage;
import com.eastrobot.converter.model.ResultCode;
import com.eastrobot.converter.model.aitype.*;
import com.eastrobot.converter.service.ConvertService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static com.eastrobot.converter.model.Constants.*;
import static com.eastrobot.converter.model.ResultCode.*;

/**
 * ConvertController
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-29 12:01
 */
@Api(tags = "转换接口")
@Slf4j
@RestController()
@RequestMapping("/convert")
public class ConvertController {

    @Autowired
    private ConvertService converterService;

    @ApiOperation("(通用接口)视频,音频,图片,转换为文本.(文件大小受限)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "待转换文件", dataType = "__file", required = true,
                    paramType = "form"),
            @ApiImplicitParam(name = "isFrameExtractKeyword", dataType = "boolean", defaultValue = "false",
                    paramType = "form",
                    value = "视频的图片解析结果是每帧提取关键字后合并的还是全部合并后提取关键字<br/>(*仅当视频文件此参数才有效)"
            )
    })
    @PostMapping(
            value = "",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseMessage<AiRecognition> recognition(MultipartFile file, HttpServletRequest request) {
        return getRecognitionResponse(file, RECOGNITION, request);
    }

    @ApiOperation("自动语音识别[ASR].(文件大小受限)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "待转换文件", dataType = "__file", required = true, paramType = "form")
    })
    @PostMapping(
            value = "/asr",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseMessage<ASR> asr(MultipartFile file, HttpServletRequest request) {
        return getRecognitionResponse(file, ASR, request);
    }

    @ApiOperation("光学图像识别[OCR].(文件大小受限)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "待转换文件", dataType = "__file", required = true, paramType = "form")
    })
    @PostMapping(
            value = "/ocr",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseMessage<OCR> ocr(MultipartFile file, HttpServletRequest request) {
        return getRecognitionResponse(file, OCR, request);
    }

    @ApiOperation("视频解析转写[VAC].(文件大小受限)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "待转换文件", dataType = "__file", required = true, paramType = "form"),
            @ApiImplicitParam(name = "isFrameExtractKeyword", dataType = "boolean", defaultValue = "false",
                    paramType = "form",
                    value = "视频的图片解析结果是每帧提取关键字后合并的还是全部合并后提取关键字<br/>"
            )
    })
    @PostMapping(
            value = "/vac",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseMessage<VAC> vac(MultipartFile file, HttpServletRequest request) {
        return getRecognitionResponse(file, VAC, request);
    }

    @ApiOperation("文本语音合成[TTS]")
    @PostMapping(
            value = "/tts",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
            consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE}
    )
    public ResponseMessage<TTS> tts(@RequestBody String text) {
        try {
            Optional.ofNullable(text).orElseThrow(BusinessException::new);
            HashMap<String, Object> ttsParam = new HashMap<>();
            ttsParam.put(IS_ASYNC_PARSE, false);
            ttsParam.put(AI_TYPE, TTS);
            ttsParam.put(AI_TTS_TEXT, text);
            ttsParam.put(AI_TTS_OPTION, new HashMap<>());

            return converterService.driver(ttsParam);
        } catch (BusinessException x) {
            return new ResponseMessage<>(ResultCode.PARAM_ERROR);
        }
    }

    private ResponseMessage getRecognitionResponse(MultipartFile file, String aiType, HttpServletRequest request) {
        if (!file.isEmpty()) {
            String sn = UUID.randomUUID().toString();
            String targetFile;
            try {
                targetFile = converterService.uploadFile(file, sn, false);
            } catch (BusinessException e) {
                return new ResponseMessage(PREPARE_UPLOAD_FILE_ERROR);
            } catch (Exception e1) {
                return new ResponseMessage(FILE_UPLOAD_FAILED);
            }

            HashMap<String, Object> recognitionParam = new HashMap<>();
            recognitionParam.put(IS_ASYNC_PARSE, false);
            recognitionParam.put(AI_IS_FRAME_EXTRACT_KEYWORD, request.getParameter(AI_IS_FRAME_EXTRACT_KEYWORD));
            recognitionParam.put(AI_RESOURCE_FILE_PATH, targetFile);
            recognitionParam.put(AI_TYPE, aiType);

            return converterService.driver(recognitionParam);
        } else {
            return new ResponseMessage(PARAM_ERROR);
        }
    }
}
