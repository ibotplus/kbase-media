package com.eastrobot.kbs.media.util.xfyun;

import com.eastrobot.kbs.media.model.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * IflytekAsrUtil
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-19 16:09
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "convert", name = "video.asr.default", havingValue = Constants.XFYUN)
public class XfyunAsrUtil {

    /**
     * {
     * "bg":250,    \\当前这句话的说话开始时间，单位为毫秒
     * "ed":2890,  \\当前这句话的说话结束时间，单位为毫秒
     * "onebest":"噢，你，你听得到我这边的声音吗？",
     * "speaker":"1" \\说话人编号（数字“1”和“2”为不同说话人，电话专用版功能）
     * }﻿
     * see http://www.xfyun.cn/doccenter/lfasr#go_sdk_doc_v2
     */
    public static JSONObject asr(String path) throws Exception {
        final String url = "http://api.xfyun.cn/v1/service/v1/iat";
        String apiKey = "da08f42480e67f574a61290717e8f945";
        String appId = "5be241a0";
        //{"engine_type":"sms16k","aue":"raw"} 固定值
        String paramBase64 = "eyJlbmdpbmVfdHlwZSI6ICJzbXMxNmsiLCJhdWUiOiJyYXcifQ==";
        String currentTimeMillis = System.currentTimeMillis() / 1000L + "";
        String md5Hex = DigestUtils.md5Hex((apiKey + currentTimeMillis + paramBase64).getBytes());

        URL httpUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-CurTime", currentTimeMillis);
        conn.setRequestProperty("X-Param", paramBase64);
        conn.setRequestProperty("X-Appid", appId);
        conn.setRequestProperty("X-CheckSum", md5Hex);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        // 设置请求 body
        conn.setDoOutput(true);
        conn.setDoInput(true);

        //设置连接超时和读取超时时间
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(20000);
        conn.connect();
        //POST请求
        OutputStream out = conn.getOutputStream();
        byte[] bytes = Base64.encodeBase64(Files.readAllBytes(Paths.get(path)));
        String body = URLEncoder.encode(new String(bytes), "utf-8");
        out.write(("audio=" + body).getBytes());
        out.flush();
        //读取响应
        InputStream is = conn.getInputStream();
        String result = IOUtils.toString(is, "utf-8");
        out.close();
        is.close();
        conn.disconnect();

        return new JSONObject(result);
    }
}
