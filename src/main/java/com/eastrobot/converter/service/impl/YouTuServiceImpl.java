package com.eastrobot.converter.service.impl;


import com.eastrobot.converter.service.YouTuService;
import com.eastrobot.converter.util.Youtu;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyManagementException;

@Service
public class YouTuServiceImpl implements YouTuService {

    private static final Logger log = LoggerFactory.getLogger(YouTuServiceImpl.class);

    @Value("${convert.image.ocr.youtu.appId}")
    private String appId;

    @Value("${convert.image.ocr.youtu.secretId}")
    private String appSecretId;

    @Value("${convert.image.ocr.youtu.secretKey}")
    private String appSecretKey;

    @Value("${convert.image.ocr.youtu.userId}")
    private String appUserId;

    @Override
    public String ocr(String image_path) throws Exception {
        String result = "";
        try {
            Youtu faceYoutu = new Youtu(appId, appSecretId, appSecretKey, Youtu.API_YOUTU_END_POINT, appUserId);
            System.out.println(image_path);
            JSONObject generalOcr = faceYoutu.GeneralOcr(image_path);
            result = handleOcrRes(generalOcr);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("orc error [%s]:", e.getMessage());
        }
        return result;
    }

    @Override
    public String ocrUlr(String image_url) throws Exception {
        String result = "";
        try {
            Youtu faceYoutu = new Youtu(appId, appSecretId, appSecretKey, Youtu.API_YOUTU_END_POINT, appUserId);
            JSONObject generalOcrUrl = faceYoutu.GeneralOcrUrl(image_url);
            result = handleOcrRes(generalOcrUrl);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String handleOcrRes(JSONObject ocrJson) throws Exception {
        StringBuffer sb = new StringBuffer();
        JSONArray jsonUrl = ocrJson.getJSONArray("items");
        for (int i = 0; i < jsonUrl.length(); i++) {
            JSONObject json = jsonUrl.getJSONObject(i);
            sb.append(json.optString("itemstring"));
        }
        return sb.toString();
    }

}
