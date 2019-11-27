/*
 * Powered by http://ibotstat.com
 */
package com.eastrobot.kbs.media.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.eastrobot.kbs.media.model.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="mailto:eko.z@outlook.com">eko.zhan</a>
 * @version v1.0
 * @date 2019/11/19 16:41
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    @Value("${convert.audio.asr.default}")
    private String asrName;
    @Value("${convert.audio.asr.xfyun.apiUrl}")
    private String asrXfyunUrl;
    @Value("${convert.audio.asr.shhan.base-url}")
    private String asrShhanUrl;
    @Value("${convert.audio.tts.default}")
    private String ttsName;
    @Value("${convert.audio.tts.m2.base-url}")
    private String ttsM2Url;
    @Value("${convert.audio.tts.baker.base-url}")
    private String ttsBakerUrl;
    @Value("${convert.image.ocr.default}")
    private String ocrName;


    @GetMapping()
    public JSONObject info(){
        JSONObject json = new JSONObject();
        json.put("asr工具", asrName);
//        if (Constants.XFYUN.equals(asrName)){
//            json.put("asr地址", asrXfyunUrl);
//        }else if (Constants.SHHAN.equals(asrName)){
//            json.put("asr地址", asrShhanUrl);
//        }
        json.put("tts工具", ttsName);
//        if (Constants.M2.equals(ttsName)){
//            json.put("tts地址", ttsM2Url);
//        }else if (Constants.DATA_BAKER.equals(ttsName)){
//            json.put("tts地址", ttsBakerUrl);
//        }
        json.put("ocr工具", ocrName);
        return json;
    }

}
