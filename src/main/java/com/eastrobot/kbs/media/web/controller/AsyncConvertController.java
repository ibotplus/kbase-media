package com.eastrobot.kbs.media.web.controller;

import com.eastrobot.kbs.media.exception.BusinessException;
import com.eastrobot.kbs.media.model.ResponseMessage;
import com.eastrobot.kbs.media.model.ResultCode;
import com.eastrobot.kbs.media.plugin.AsyncMode;
import com.eastrobot.kbs.media.service.ConvertService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * AsyncConvertController
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-29 12:01
 */
@Api(tags = "转换接口(异步)")
@Slf4j
@RestController
@ConditionalOnBean(AsyncMode.class)
public class AsyncConvertController {

    @Autowired
    private ConvertService converterService;

    @ApiOperation(value = "上传视频,音频,图片,转换为文本.(异步模式上传为压缩文件)", response = ResponseMessage.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "待转换文件", dataType = "__file", required = true, paramType = "form")
    })
    @PostMapping(
            value = "/convertAsync",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseMessage convertAsync(@RequestParam(value = "file") MultipartFile file) {
        try {
            Optional.ofNullable(file).filter(v -> !v.isEmpty()).orElseThrow(BusinessException::new);
            String md5 = DigestUtils.md5Hex(file.getBytes());
            try {
                converterService.uploadFile(file, md5, true);
            } catch (Exception e) {
                return new ResponseMessage(ResultCode.FILE_UPLOAD_FAILURE);
            }

            return new ResponseMessage(ResultCode.SUCCESS, md5);
        } catch (Exception e) {
            return new ResponseMessage(ResultCode.PARAM_ERROR);
        }
    }

    @ApiOperation(value = "通过md5来获得解析结果.", response = ResponseMessage.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sn", value = "异步模式上传文件返回的md5", dataType = "string", required = true, paramType =
                    "path")
    })
    @GetMapping(
            value = "/convertAsync/{md5}",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE}
    )
    public ResponseMessage convertAsync(@PathVariable String md5) {
        Optional<String> md5Op = Optional.ofNullable(md5).filter(StringUtils::isNotBlank);
        if (md5Op.isPresent()) {
            return converterService.findParseResultByMd5(md5, true);
        } else {
            return new ResponseMessage(ResultCode.PARAM_ERROR);
        }
    }

}
