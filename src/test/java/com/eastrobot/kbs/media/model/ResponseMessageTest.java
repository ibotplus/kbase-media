package com.eastrobot.kbs.media.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.eastrobot.kbs.media.exception.BusinessException;
import com.eastrobot.kbs.media.model.aitype.ASR;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.UUID;


@RunWith(JUnit4.class)
public class ResponseMessageTest {

    @Test
    public void testObj() {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResultCode(ResultCode.CFG_ERROR);
        responseMessage.setMd5(UUID.randomUUID().toString());


        System.out.println(JSON.toJSONString(responseMessage));
    }

    @Test
    public void testJson() throws Exception {
        String line = Files.lines(Paths.get("E:\\converter-output\\c6510693fcc4e654c62de4c432bc5578.rs"),
                Charset.defaultCharset()).reduce("", (a, b) -> a + b);

        JSONObject contentJson = Optional.ofNullable(line)
                .filter(StringUtils::isNotBlank)
                .map(JSONObject::parseObject)
                .orElseThrow(BusinessException::new);
        AiType aiType = AiType.valueOf(contentJson.getString("aiType"));

        ResponseMessage<ASR> resp = JSONObject.parseObject(line,
                new TypeReference<ResponseMessage<ASR>>() {
                });

        System.out.println(resp);
    }

    @Test
    public void testSplit() {
        String text = "12345。54321。";
        ArrayList<String> result = new ArrayList<>();
        while (text.length() > 5) {
            String value = text.substring(0, Math.min(5, text.length())); //left
            text = text.substring(5); //right
            if (!value.endsWith("。")) {
                value = value + text.substring(0, text.indexOf("。") + 1);
                text = text.substring(text.indexOf("。") + 1);
            }
            result.add(value);
        }

        if (text.length() < 100) {
            result.add(text);
        }
        System.out.println(result);
    }

    @Test
    public void testString() {
        String text = "12345。54321。";
        StringTokenizer tokenizer = new StringTokenizer(text, "。", false);
        System.out.println(tokenizer.countTokens());
        while (tokenizer.hasMoreElements()) {
            System.out.println(tokenizer.nextElement());
        }
    }
}