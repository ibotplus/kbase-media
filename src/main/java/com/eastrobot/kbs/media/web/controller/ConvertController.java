package com.eastrobot.kbs.media.web.controller;

import com.eastrobot.kbs.media.exception.BusinessException;
import com.eastrobot.kbs.media.model.AiType;
import com.eastrobot.kbs.media.model.ResponseMessage;
import com.eastrobot.kbs.media.model.ResultCode;
import com.eastrobot.kbs.media.model.aitype.ASR;
import com.eastrobot.kbs.media.model.aitype.OCR;
import com.eastrobot.kbs.media.model.aitype.TTS;
import com.eastrobot.kbs.media.model.aitype.VAC;
import com.eastrobot.kbs.media.service.ConvertService;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static com.eastrobot.kbs.media.model.Constants.*;

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

    @ApiOperation("(AI识别通用接口)视频,音频,图片,转换为文本.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "待转换文件", dataType = "__file", required = true, paramType = "form"),
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
    public ResponseMessage recognition(MultipartFile file, HttpServletRequest request) {
        return getRecognitionResponse(file, AiType.GENERIC_RECOGNITION, request);
    }

    @ApiOperation("自动语音识别[ASR].")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "待转换文件", dataType = "__file", required = true, paramType = "form")
    })
    @PostMapping(
            value = "/asr",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseMessage<ASR> asr(MultipartFile file, HttpServletRequest request) {
        return getRecognitionResponse(file, AiType.ASR, request);
    }

    @ApiOperation("光学图像识别[OCR].")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "待转换文件", dataType = "__file", required = true, paramType = "form")
    })
    @PostMapping(
            value = "/ocr",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseMessage<OCR> ocr(MultipartFile file, HttpServletRequest request) {
        return getRecognitionResponse(file, AiType.OCR, request);
    }

    @ApiOperation("视频解析转写[VAC].")
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
        return getRecognitionResponse(file, AiType.VAC, request);
    }

    @ApiOperation("文本语音合成[TTS]")
    @PostMapping(
            value = "/tts",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
            consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE}
    )
    public ResponseMessage<TTS> tts(@RequestBody String text) {
        try {
            Optional.ofNullable(text).filter(StringUtils::isNotBlank).orElseThrow(BusinessException::new);
            Map<String, Object> ttsParam = ImmutableMap.<String, Object>builder()
                    .put(IS_ASYNC_PARSE, false)
                    .put(AI_TYPE, AiType.TTS)
                    .put(AI_TTS_TEXT, text)
                    .put(AI_TTS_OPTION, Collections.emptyMap())
                    .put(AI_RESOURCE_FILE_PATH, DigestUtils.md5Hex(text))
                    .build();

            return converterService.driver(ttsParam);
        } catch (BusinessException e) {
            return new ResponseMessage<>(ResultCode.PARAM_ERROR);
        }
    }

    private ResponseMessage getRecognitionResponse(MultipartFile file, AiType aiType, HttpServletRequest request) {
        try {
            Optional.ofNullable(file).filter(v -> !v.isEmpty()).orElseThrow(BusinessException::new);
            String md5 = DigestUtils.md5Hex(file.getBytes());
            String targetFile;
            try {
                targetFile = converterService.uploadFile(file, md5, false);
            } catch (Exception e) {
                return new ResponseMessage(ResultCode.FILE_UPLOAD_FAILURE);
            }

            Map<String, Object> recognitionParam = ImmutableMap.<String, Object>builder()
                    .put(IS_ASYNC_PARSE, false)
                    .put(AI_IS_FRAME_EXTRACT_KEYWORD, Optional.ofNullable(request.getParameter(AI_IS_FRAME_EXTRACT_KEYWORD)).orElse(""))
                    .put(AI_RESOURCE_FILE_PATH, targetFile)
                    .put(AI_TYPE, aiType)
                    .build();

            return converterService.driver(recognitionParam);
        } catch (Exception e) {
            return new ResponseMessage<>(ResultCode.PARAM_ERROR);
        }
    }
}
