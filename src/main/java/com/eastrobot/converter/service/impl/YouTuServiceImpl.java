package com.eastrobot.converter.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eastrobot.converter.service.YouTuService;
import com.eastrobot.converter.util.youtu.YouTu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class YouTuServiceImpl implements YouTuService {

    @Value("${convert.image.ocr.youtu.appId}")
    private String appId;

    @Value("${convert.image.ocr.youtu.secretId}")
    private String appSecretId;

    @Value("${convert.image.ocr.youtu.secretKey}")
    private String appSecretKey;

    @Value("${convert.image.ocr.youtu.userId}")
    private String appUserId;

    @Override
    public String ocr(String imagePath) throws Exception {
        YouTu faceYoutu = new YouTu(appId, appSecretId, appSecretKey, YouTu.API_YOUTU_END_POINT, appUserId);
        JSONObject ocrJson = faceYoutu.generalOcr(imagePath);

        log.debug("ocr {} result: {}", imagePath, ocrJson);

        StringBuffer sb = new StringBuffer();
        if ("0".equals(ocrJson.getString("errorcode"))) {
            JSONArray items = ocrJson.getJSONArray("items");
            for (int i = 0; i < items.size(); i++) {
                JSONObject json = items.getJSONObject(i);
                sb.append(json.getString("itemstring"));
            }

            return sb.toString().trim();
        } else {
            throw new Exception("parse image failed: " + ocrJson.get("errormsg"));
        }
    }

}
