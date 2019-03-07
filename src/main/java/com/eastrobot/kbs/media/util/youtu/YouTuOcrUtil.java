package com.eastrobot.kbs.media.util.youtu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eastrobot.kbs.media.model.Constants;
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
        log.info("initialize youtu ocr tools complete.");
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

    /**
     * 根据指定的图片路径，鉴定该图片是否是色情图片，normal_hot_porn>0.3
     *
     * @return java.lang.String {"normal":0.53598434,"female-genital":0.0,"normal_hot_porn":0.15097368,"anus":0.0,
     * "sex":0.0,"pubes":0.0,"hot":0.46401528,"errormsg":"OK","porn":3.5230076E-7,"male-genital":0.0,
     * "female-breast":1.0,"faces":[],"errorcode":0}
     *
     * @author eko.zhan
     * @date 2019/3/7 12:34
     */
    public static String porn(String imagePath) throws Exception {
        JSONObject json = faceYoutu.imagePorn(imagePath);
        json.remove("feas");
        json.getJSONArray("tags").forEach(item -> {
            JSONObject tag = JSON.parseObject(item.toString());
            json.put(tag.getString("tag_name"), tag.getFloatValue("tag_confidence_f"));
        });
        json.remove("tags");
        return json.toJSONString();
    }

    /**
     * 根据指定的图片路径，鉴定该图片是否是色情图片，normal_hot_porn>0.3
     *
     * @return java.lang.String {"normal":0.53598434,"female-genital":0.0,"normal_hot_porn":0.15097368,"anus":0.0,
     * "sex":0.0,"pubes":0.0,"hot":0.46401528,"errormsg":"OK","porn":3.5230076E-7,"male-genital":0.0,
     * "female-breast":1.0,"faces":[],"errorcode":0}
     *
     * @author eko.zhan
     * @date 2019/3/7 12:34
     */
    public static String terrorism(String imagePath) throws Exception {
        return faceYoutu.imageTerrorism(imagePath).toString();
    }

}
