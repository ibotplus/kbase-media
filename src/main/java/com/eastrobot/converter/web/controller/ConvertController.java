package com.eastrobot.converter.web.controller;

import com.eastrobot.converter.exception.BusinessException;
import com.eastrobot.converter.model.ResponseMessage;
import com.eastrobot.converter.model.ResultCode;
import com.eastrobot.converter.service.ConvertService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * ConvertController
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-29 12:01
 */
@Api(tags = "转换接口")
@Slf4j
@RestController
public class ConvertController {

    @Autowired
    private ConvertService converterService;

    @ApiOperation(value = "上传视频,音频,图片,转换为文本.(文件大小受限)", response = ResponseMessage.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "待转换文件", dataType = "__file", required = true, paramType = "form"),
            @ApiImplicitParam(name = "isFrameExtractKeyword", dataType = "boolean", defaultValue = "false", paramType = "form",
                    value = "视频的图片解析结果是每帧提取关键字后合并的还是全部合并后提取关键字<br/>(*仅当视频文件此参数才有效)"
            )
    })
    @PostMapping(
            value = "/convert",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseMessage convert(@RequestParam("file") MultipartFile file,
                                   @RequestParam("isFrameExtractKeyword") Boolean isFrameExtractKeyword) {
        if (!file.isEmpty()) {
            String sn = UUID.randomUUID().toString();
            String targetFile;
            try {
                targetFile = converterService.doUpload(file, sn, false);
            } catch (BusinessException e) {
                return new ResponseMessage(ResultCode.PREPARE_UPLOAD_FILE_ERROR, e.getMessage());
            } catch (Exception e1) {
                return new ResponseMessage(ResultCode.FILE_UPLOAD_FAILED);
            }

            return converterService.driver(targetFile, isFrameExtractKeyword, false);
        } else {
            return new ResponseMessage(ResultCode.PARAM_ERROR);
        }
    }

    @ApiOperation(value = "文本语音合成(文本长度)", response = ResponseMessage.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "text", value = "待转语音文本内容，使用UTF-8编码。小于512个中文字或者英文数字", dataType = "string", defaultValue = "", required = true, paramType = "form"),
            @ApiImplicitParam(name = "param", dataType = "string", defaultValue = "", paramType = "form",
                    value = "语音合成可选参数<br/>spd:语速，取值0-9，默认为5中语速,<br/>pit:音调，取值0-9，默认为5中语调,<br/>vol:音量，取值0-15，默认为5中音量,<br/>per:发音人选择, 0为女声，1为男声，3为情感合成-度逍遥，4为情感合成-度丫丫，默认为普通女"
            )
    })
    @PostMapping(
            value = "/convert/tts",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseMessage convertTts(@RequestParam("text") String text,
                                      @RequestParam("param") String param){

        if (StringUtils.isNoneBlank(text)){
            return converterService.driver(text,false);
        }else {
            return new ResponseMessage(ResultCode.PARAM_ERROR);
        }

    }
}
