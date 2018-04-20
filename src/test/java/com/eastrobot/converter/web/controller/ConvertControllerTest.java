package com.eastrobot.converter.web.controller;

import com.eastrobot.converter.util.HttpClientUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConvertControllerTest {

    @Test
    public void driver() {
        HttpPost httpPost = new HttpPost("http://172.16.23.12/converter-api/convert");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        // 上传的文件
        builder.addBinaryBody("uploadFile", new File("C:\\Users\\User\\Desktop\\xPixel.jpg"));
        // 设置其他参数
        // builder.addTextBody("type", "keyword", ContentType.TEXT_PLAIN.withCharset("UTF-8"));
        HttpEntity httpEntity = builder.build();
        httpPost.setEntity(httpEntity);
        HttpClient httpClient = HttpClients.createDefault();
        try {
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void convert() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("uploadFile", new File("C:\\Users\\User\\Desktop\\xPixel.jpg"));
        String result = HttpClientUtil.doPost("http://172.16.23.12/converter-api/convert", map);
    }
}