package com.eastrobot.kbs.media.service.impl;

import com.eastrobot.kbs.media.model.Constants;
import com.eastrobot.kbs.media.model.ParseResult;
import com.eastrobot.kbs.media.model.ResultCode;
import com.eastrobot.kbs.media.model.aitype.TTS;
import com.eastrobot.kbs.media.service.TtsParserCallBack;
import com.eastrobot.kbs.media.service.TtsService;
import com.eastrobot.kbs.media.util.DataBakerUtil;
import com.eastrobot.kbs.media.util.baidu.BaiduAsrUtils;
import com.eastrobot.kbs.media.util.concurrent.ThreadPoolUtil;
import com.eastrobot.kbs.media.util.m2.M2TtsUtil;
import com.eastrobot.kbs.media.util.youtu.Base64Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ForkJoinPool;

/**
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2019-06-06 9:11
 */
@Slf4j
@Service
public class TtsServiceImpl implements TtsService {

    @Value("${convert.audio.tts.default}")
    private String audioTool;

    @Value("${convert.audio.tts.max-text-length}")
    private int maxTextLength;

    @Override
    public ParseResult<TTS> handle(String text, Map<String, Object> ttsOption) {
        if (Constants.BAIDU.equals(audioTool)) {
            return TtsParserTemplate.handle(text, maxTextLength, ttsOption, this::baiduTtsHandler);
        } else if (Constants.M2.equals(audioTool)) {
            return TtsParserTemplate.handle(text, maxTextLength, ttsOption, this::m2TtsHandler);
        } else if (Constants.DATA_BAKER.equals(audioTool)) {
            return TtsParserTemplate.handle(text, maxTextLength, ttsOption, this::dataBakerTtsHandler);
        } else {
            return new ParseResult<>(ResultCode.PARSE_EMPTY, null);
        }
    }

    private String baiduTtsHandler(String text, Map<String, Object> options) {
        //String tex = "每次启动和定时器每天晚上校验 license";
        String lan = "zh";// 固定值zh。语言选择,目前只有中英文混合模式，填写固定值zh
        int ctp = 1; // 客户端类型选择，web端填写固定值1

        byte[] tts = BaiduAsrUtils.tts(text, lan, ctp, (HashMap<String, Object>) options);
        return Base64Util.encode(tts);
    }

    private String m2TtsHandler(String text, Map<String, Object> options) {
        byte[] tts = M2TtsUtil.tts(text);
        return Base64Util.encode(tts);
    }

    private String dataBakerTtsHandler(String text, Map<String, Object> options) {
        byte[] tts = DataBakerUtil.tts(text);
        return Base64Util.encode(tts);
    }

    private static class TtsParserTemplate {

        private static ParseResult<TTS> handle(String text, int maxTextLength, Map<String, Object> ttsOption,
                                               TtsParserCallBack callBack) {
            HashMap<String, Object> options = new HashMap<>();
            if (!ttsOption.isEmpty()) {
                options = (HashMap<String, Object>) ttsOption;
            }

            // fork-join text content
            ForkJoinPool forkJoinPool = ThreadPoolUtil.ofForkJoin();
            List<String> splitList = forkJoinPool.invoke(new SplitTextTask(text, 0, text.length() - 1, maxTextLength));

            List<String> resultList = new ArrayList<>(splitList.size());
            for (String sText : splitList) {
                String result = null;
                try {
                    result = callBack.doInParser(sText, options);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                resultList.add(result);
            }
            String result = resultList.stream().reduce("", (a, b) -> a = a + b);

            return new ParseResult<>(ResultCode.SUCCESS, new TTS(text, result));
        }
    }
}
