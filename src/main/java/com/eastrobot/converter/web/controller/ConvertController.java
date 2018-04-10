package com.eastrobot.converter.web.controller;

import com.alibaba.fastjson.JSON;
import com.eastrobot.converter.model.Constants;
import com.eastrobot.converter.model.ResponseEntity;
import com.eastrobot.converter.model.ResponseMessage;
import com.eastrobot.converter.model.ResultCode;
import com.eastrobot.converter.service.ConvertService;
import com.eastrobot.converter.util.ResourceUtil;
import com.hankcs.hanlp.HanLP;
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

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ConvertController
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-29 12:01
 */
@Api(tags = "视频音频图片转换接口")
@Slf4j
@RestController
public class ConvertController {

    @Autowired
    private ConvertService converterService;

    @ApiOperation(value = "上传视频,音频,图片,转换为文本.", response = ResponseMessage.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "待转换文件", dataType = "__file", required = true, paramType = "form"),
            @ApiImplicitParam(name = "type", value = "转换类型(可选:keyword:关键字(10个);fulltext:全文)", defaultValue = "fulltext",
                    paramType = "form", allowableValues = "keyword, fulltext")
    })
    @PostMapping(
            value = "/driver",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public String driver(@RequestParam(value = "file") MultipartFile file,
                             @RequestParam(value = "type", required = false, defaultValue = "fulltext") String type) {

        String sn = UUID.randomUUID().toString();
        String inputFile = converterService.getDefaultOutputFolderPath(sn) + File.separator + file
                .getOriginalFilename();
        if (!file.isEmpty()) {
            try {
                File tmpFile = new File(inputFile);
                tmpFile.mkdirs();
                file.transferTo(tmpFile);
            } catch (Exception e) {
                log.error("file upload error!", e);
                ResponseMessage responseMessage = new ResponseMessage(ResultCode.FILE_UPLOAD_FAILED);

                return JSON.toJSONString(responseMessage);
            }

            ResponseMessage responseMessage = converterService.driver(inputFile);
            responseMessage.setSn(sn);
            if (Constants.KEYWORD.equals(type)) {
                // extract keyword
                Optional.of(responseMessage)
                        .map((ResponseMessage::getResponseEntity))
                        .map(ResponseEntity::getImageContent)
                        .ifPresent((value) -> {
                            List<String> imagekeyword = HanLP.extractKeyword(value, 10);
                            String keyword = ResourceUtil.list2String(imagekeyword, "");
                            responseMessage.getResponseEntity().setImageKeyword(keyword);
                        });
                Optional.of(responseMessage)
                        .map((ResponseMessage::getResponseEntity))
                        .map(ResponseEntity::getAudioContent)
                        .ifPresent((value) -> {
                            List<String> audioKeyword = HanLP.extractKeyword(value, 10);
                            String keyword = ResourceUtil.list2String(audioKeyword, "");
                            responseMessage.getResponseEntity().setAudioKeyword(keyword);
                        });
            }

            return JSON.toJSONString(responseMessage);
        } else {
            ResponseMessage responseMessage = new ResponseMessage(ResultCode.PARAM_ERROR);

            return JSON.toJSONString(responseMessage);
        }
    }
}
