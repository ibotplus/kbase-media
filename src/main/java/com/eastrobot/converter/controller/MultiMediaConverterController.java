package com.eastrobot.converter.controller;

import com.alibaba.fastjson.JSONObject;
import com.eastrobot.converter.service.MultiMediaConverterService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

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
     *             return demo
     *
     *             // return for success
     *             {
     *              "sn":"Serial Number",
     *              "flag":"success",
     *              "file_type": "image", // or video or audio
     *              "content": "parse result(video or audio) content", // with image equals keyword
     *              "keyword": "limit to 200 keyword"
     *             }
     *
     *             // return for failed
     *             {
     *              "sn":"Serial Number",
     *              "flag":"failed",
     *              "err_code": 2000,
     *              "err_msg": "data empty."
     *             }
     *             </pre>
     *
     * @author Yogurt_lei
     * @date 2018-03-29 13:23
     */
    @ApiOperation("上传视频,音频,图片文件,转换为文本.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "传入的文件", required = true, paramType = "multipartFile")
    })
    @ApiResponses({
            @ApiResponse(code = 400, message = "请求参数没填好"),
            @ApiResponse(code = 404, message = "请求路径没有或页面跳转路径不对")
    })
    @RequestMapping("/driver")
    public JSONObject driver(@RequestParam("file") MultipartFile file) {
        String folder = converterService.getDefaultOutputFolderPath();
        String inputFile = folder + File.separator + file.getOriginalFilename();
        JSONObject resultJson = new JSONObject();
        if (!file.isEmpty()) {
            try {
                File f = new File(inputFile);
                f.mkdirs();
                file.transferTo(f);
            } catch (Exception e) {
                resultJson.put("flag", "failed");
                resultJson.put("err_code", "1000");
                resultJson.put("err_msg", e.getMessage());

                return resultJson;
            }
        }

        return converterService.driver(inputFile);
    }
}
