package com.eastrobot.converter.web.controller;

import com.eastrobot.converter.model.ResponseMessage;
import com.eastrobot.converter.model.ResultCode;
import com.eastrobot.converter.service.ConvertService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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

    @ApiOperation(value = "上传视频,音频,图片,转换为文本.", response = ResponseMessage.class)
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
            } catch (Exception e) {
                log.error("file upload error!", e);

                return new ResponseMessage(ResultCode.FILE_UPLOAD_FAILED);
            }

            return converterService.driver(targetFile, isFrameExtractKeyword, false);
        } else {
            return new ResponseMessage(ResultCode.PARAM_ERROR);
        }
    }
}
