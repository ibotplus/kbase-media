package com.eastrobot.converter.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.eastrobot.converter.model.ErrorCode;
import com.eastrobot.converter.service.MultiMediaConverterService;
import io.swagger.annotations.*;
import lombok.Lombok;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * MutilMediaConverterController
 *
 * @author <a href="yogurt.lei@xiaoi.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-29 12:01
 */
@Api(tags = "视频音频图片转换接口")
@RestController
public class MultiMediaConverterController {

    @Autowired
    private MultiMediaConverterService converterService;

    /**
     * 当前为同步接口 后期优化为异步
     *
     * @param file input file
     *             <pre>
     *                         return demo
     *
     *                         // return for success
     *                         {
     *                          "sn":"Serial Number",
     *                          "flag":"success",
     *                          "file_type": "image", // or video or audio
     *                          "content": "parse result(video or audio) content", // with image equals keyword
     *                          "keyword": "limit to 200 keyword"
     *                         }
     *
     *                         // return for failed
     *                         {
     *                          "sn":"Serial Number",
     *                          "flag":"failed",
     *                          "err_code": 2000,
     *                          "err_msg": "data empty."
     *                         }
     *                         </pre>
     *
     * @author Yogurt_lei
     * @date 2018-03-29 13:23
     */
    @ApiOperation("上传视频,音频,图片文件,转换为文本.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "file", value = "传入待转换文件", required = true, dataType = "file", paramType = "body"),
        @ApiImplicitParam(name = "type", value = "转换类型", defaultValue = "keyword", dataType = "String", paramType = "body")
    })
    @ApiResponses({
        @ApiResponse(code = 0, message = "参数不正确"),
    })
    @PostMapping(value = "/driver",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public JSONObject driver(@RequestParam("file") MultipartFile file, @RequestParam(value = "type", required = false) String type) {
        //CommonsMultipartFile 为何不能接收

        String folder = converterService.getDefaultOutputFolderPath();
        String inputFile = folder + File.separator + file.getOriginalFilename();
        JSONObject resultJson = new JSONObject();
        if (!file.isEmpty()) {
            try {
                String fileName = file.getOriginalFilename();
                byte[] bytes = file.getBytes();
                BufferedOutputStream buffStream =
                        new BufferedOutputStream(new FileOutputStream(new File("/tmp/" + fileName)));
                buffStream.write(bytes);
                buffStream.close();
                // File f = new File(inputFile);
                // if (f.mkdirs()) {
                //     file.transferTo(f);
                // }
            } catch (Exception e) {
                resultJson.put("flag", "failed");
                resultJson.put("err_code", "1000");
                resultJson.put("err_msg", e.getMessage());

                return resultJson;
            }
        }

        ErrorCode errorCode = ErrorCode.SUCCESS;
        errorCode.getCode();
        Lombok l = new Lombok();

        return converterService.driver(inputFile);
    }
}
