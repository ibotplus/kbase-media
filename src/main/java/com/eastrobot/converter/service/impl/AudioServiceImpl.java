package com.eastrobot.converter.service.impl;

import com.eastrobot.converter.model.Constants;
import com.eastrobot.converter.model.ParseResult;
import com.eastrobot.converter.service.AudioService;
import com.eastrobot.converter.util.baidu.BaiduAsrUtils;
import com.eastrobot.converter.util.shhan.ShhanAsrUtil;
import com.eastrobot.converter.util.xfyun.XfyunAsrConstants;
import com.eastrobot.converter.util.xfyun.XfyunAsrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.util.HashMap;

import static com.eastrobot.converter.model.ResultCode.*;
import static com.eastrobot.converter.util.baidu.BaiduAsrConstants.PCM;
import static com.eastrobot.converter.util.baidu.BaiduAsrConstants.RATE;


/**
 * AudioServiceImpl
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 18:54
 */
@Slf4j
@Service
public class AudioServiceImpl implements AudioService {

    @Value("${convert.audio.asr.default}")
    private String audioTool;

    @Autowired
    private AudioParserTemplate audioParserTemplate;

    @Override
    public ParseResult handle(String audioFilePath) {
        if (new File(audioFilePath).exists()) {
            if (Constants.BAIDU.equals(audioTool)) {
                return audioParserTemplate.handle(audioFilePath, this::baiduAsrHandler);
            } else if (Constants.SHHAN.equals(audioTool)) {
                return audioParserTemplate.handle(audioFilePath, this::shhanAsrHandler);
            } else if (Constants.XFYUN.equals(audioTool)) {
                return audioParserTemplate.handle(audioFilePath, this::xfyunAsrHandler);
            } else {
                return new ParseResult(CFG_ERROR, CFG_ERROR.getMsg(), "", "",null);
            }
        } else {
            return new ParseResult(ASR_FAILURE, "提取到空的音频流", "", "",null);
        }
    }

    @Override
    public ParseResult handleTts(String text) {
        if(StringUtils.isNoneBlank(text)){
            byte[] data = baiduTtsHandler(text);
            if(data!=null){
                //对字节数组Base64编码
                BASE64Encoder encoder = new BASE64Encoder();//encoder.encode(data)
                return new ParseResult(SUCCESS, SUCCESS.getMsg(), "", "",data);
            }else{
                return new ParseResult(PARSE_EMPTY, PARSE_EMPTY.getMsg(), "", "",null);
            }
         }else {
            return new ParseResult(TTS_FAILURE, "文本内容为空", "", "",null);
        }
    }

    private String baiduAsrHandler(String audioFilePath) throws Exception {
        JSONObject asr = BaiduAsrUtils.asr(audioFilePath, PCM, RATE);
        if (asr.optInt("err_no", -1) == 0) {
            //数组字符串
            String result = asr.optString("result");
            return StringUtils.substringBetween(result, "[\"", "\"]").trim();
        } else {
            throw new Exception(asr.getString("err_msg"));
        }
    }

    private String shhanAsrHandler(String audioFilePath) throws Exception {
        String asr = ShhanAsrUtil.asr(audioFilePath);
        if (StringUtils.isNotBlank(asr)) {
            return asr;
        } else {
            throw new Exception("empty result");
        }
    }

    private String xfyunAsrHandler(String audioFilePath) throws Exception {
        com.alibaba.fastjson.JSONObject asr = XfyunAsrUtil.asr(audioFilePath);
        if (asr.getString(XfyunAsrConstants.ERROR_CODE).equals(XfyunAsrConstants.SUCCESS)) {
            return asr.getString(XfyunAsrConstants.MESSAGE);
        } else {
            throw new Exception(asr.toString());
        }
    }

    private byte[] baiduTtsHandler(String tex){
        //String tex = "每次启动和定时器每天晚上校验 license";
        String lan="zh";// 固定值zh。语言选择,目前只有中英文混合模式，填写固定值zh
        int ctp = 1; // 客户端类型选择，web端填写固定值1
        HashMap<String, Object> options = new HashMap<String, Object>();
        return BaiduAsrUtils.tts(tex,lan,ctp,options);
    }
}
