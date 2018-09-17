package com.eastrobot.kbs.media.util.youtu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eastrobot.kbs.media.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * YouTu
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-08 15:00
 */
@Slf4j
public class YouTu {
    public final static String API_YOUTU_END_POINT = "https://api.youtu.qq.com/youtu/";

    //30 day
    private static int EXPIRED_SECONDS = 2592000;
    private String m_appid;
    private String m_secret_id;
    private String m_secret_key;
    private String m_end_point;
    private String m_user_id;

    public YouTu(String appid, String secret_id, String secret_key, String end_point, String user_id) {
        m_appid = appid;
        m_secret_id = secret_id;
        m_secret_key = secret_key;
        m_end_point = end_point;
        m_user_id = user_id;
    }

    /**
     * 通用印刷体文字识别
     * see http://open.youtu.qq.com/#/develop/api-ocr-general
     */
    public JSONObject generalOcr(String image_path) throws IOException {
        JSONObject data = new JSONObject();

        data.put("image", getBase64FromFile(image_path));
        data.put("app_id", m_appid);

        return  sendHttpsRequest(data, "ocrapi/generalocr");
    }

    private String getBase64FromFile(String filePath) throws IOException {
        File imageFile = new File(filePath);
        if (imageFile.exists()) {
            byte data[] = FileUtils.readFileToByteArray(new File(filePath));
            return Base64Util.encode(data);
        } else {
            throw new FileNotFoundException(filePath + " not exist");
        }
    }

    private JSONObject sendHttpsRequest(JSONObject postData, String method) throws IOException {

        String mySign = YoutuSign.getSignature(m_appid, m_secret_id, m_secret_key,
                System.currentTimeMillis() / 1000 + EXPIRED_SECONDS, m_user_id);

        HttpClient httpClient = HttpClientUtil.getHttpClient();
        HttpPost httpPost = new HttpPost(m_end_point + method);
        httpPost.setHeader("user-agent", "youtu-java-sdk");
        httpPost.setHeader("Authorization", mySign.toString());
        httpPost.setHeader("accept", "*/*");
        httpPost.setHeader("user-agent", "youtu-java-sdk");
        httpPost.setHeader("Content-Type", "text/json");
        httpPost.setEntity(new StringEntity(postData.toString(), Charset.forName("utf-8")));

        ResponseHandler<String> responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, Charset.forName("utf-8")) : null;
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };

        String result = httpClient.execute(httpPost, responseHandler);

        return JSON.parseObject(result);
    }
}
