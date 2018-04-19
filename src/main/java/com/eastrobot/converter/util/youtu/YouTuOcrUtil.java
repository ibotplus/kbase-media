package com.eastrobot.converter.util.youtu;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eastrobot.converter.model.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "convert", name = "image.ocr.default", havingValue = Constants.YOUTU)
public class YouTuOcrUtil {

    @Value("${convert.image.ocr.youtu.appId}")
    private String appId;

    @Value("${convert.image.ocr.youtu.secretId}")
    private String appSecretId;

    @Value("${convert.image.ocr.youtu.secretKey}")
    private String appSecretKey;

    @Value("${convert.image.ocr.youtu.userId}")
    private String appUserId;

    private static YouTu faceYoutu;

    @PostConstruct
    private void init() {
        faceYoutu = new YouTu(appId, appSecretId, appSecretKey, YouTu.API_YOUTU_END_POINT, appUserId);
    }

    public static String ocr(String imagePath) throws Exception {
        JSONObject ocrJson = faceYoutu.generalOcr(imagePath);
        StringBuilder sb = new StringBuilder();
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
