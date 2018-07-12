package com.eastrobot.converter.model;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.UUID;


@RunWith(JUnit4.class)
public class ResponseMessageTest {

    @Test
    public void testObj() {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResultCode(ResultCode.CFG_ERROR);
        responseMessage.setSn(UUID.randomUUID().toString());


        System.out.println(JSON.toJSONString(responseMessage));
    }
}