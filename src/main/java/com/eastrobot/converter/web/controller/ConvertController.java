package com.eastrobot.converter.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eastrobot.converter.model.ErrorCode;
import com.eastrobot.converter.model.ResponseMessage;
import com.eastrobot.converter.service.ConvertService;
import com.eastrobot.converter.util.JsonMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

/**
 * ConvertController
 *
 * @author <a href="yogurt.lei@xiaoi.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-29 12:01
 */
@Api(tags = "视频音频图片转换接口")
@RestController
public class ConvertController {

    @Autowired
    private ConvertService converterService;

    @ApiOperation(value = "上传视频,音频,图片文件,转换为文本.", response = ResponseMessage.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "file", value = "待转换文件", dataType = "__file", required = true, paramType = "form"),
        @ApiImplicitParam(name = "type", value = "转换类型(可选:keyword:关键字;fulltext:全文)", defaultValue = "keyword", paramType = "form",allowableValues = "keyword, fulltext")
    })
    @PostMapping(
        value = "/driver",
        produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public JSONObject driver(@RequestParam(value = "file", required = false) MultipartFile file,
                             @RequestParam(value = "type", required = false) String type) {

        String sn = UUID.randomUUID().toString();

        String folder = converterService.getDefaultOutputFolderPath();
        String inputFile = folder + File.separator + file.getOriginalFilename();
        JSONObject resultJson = new JSONObject();
        if (!file.isEmpty()) {
            try {
                File tmpFile = new File(inputFile);
                tmpFile.mkdirs();
                file.transferTo(tmpFile);
            } catch (Exception e) {
                String json = new JsonMapper().toJson(new ResponseMessage(ErrorCode.FAILURE));
                return JSON.parseObject(json);
            }
        }

        return converterService.driver(inputFile);
    }
}
