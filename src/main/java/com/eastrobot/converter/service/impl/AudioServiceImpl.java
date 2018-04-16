package com.eastrobot.converter.service.impl;

import com.eastrobot.converter.model.Constants;
import com.eastrobot.converter.model.ParseResult;
import com.eastrobot.converter.service.AudioService;
import com.eastrobot.converter.util.baidu.BaiduSpeechUtils;
import com.eastrobot.converter.util.shhan.ShhanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.eastrobot.converter.model.ResultCode.CFG_ERROR;
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
        if (Constants.BAIDU.equals(audioTool)) {
            return audioParserTemplate.handle(this::baiduAsrHandler, audioFilePath);
        } else if (Constants.SHHAN.equals(audioTool)) {
            return audioParserTemplate.handle(this::shhanAsrHandler, audioFilePath);
        } else {
            return new ParseResult(CFG_ERROR, "", "");
        }
    }

    private String baiduAsrHandler(String audioFilePath) throws Exception {
        JSONObject asr = BaiduSpeechUtils.asr(audioFilePath, PCM, RATE);
        if (asr.optInt("err_no", -1) == 0) {
            //数组字符串
            String result = asr.optString("result");
            result = StringUtils.substringBetween(result, "[\"", "\"]");

            if (StringUtils.isBlank(result)) {
                throw new Exception("empty result");
            }

            return result;
        } else {
            throw new Exception(asr.getString("err_msg"));
        }
    }

    private String shhanAsrHandler(String audioFilePath) throws Exception {
        String asr = ShhanUtil.asr(audioFilePath);
        if (StringUtils.isNotBlank(asr)) {
            return asr;
        } else {
            throw new Exception("empty result");
        }
    }
}
