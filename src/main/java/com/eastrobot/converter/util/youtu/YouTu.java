package com.eastrobot.converter.util.youtu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * YouTu
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-08 15:00
 */
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
     * see http://open.youtu.qq.com/#/develop/api-ocr-general
     */
    public JSONObject generalOcr(String image_path) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        JSONObject data = new JSONObject();

        data.put("image", getBase64FromFile(image_path));
        data.put("app_id", m_appid);

        JSONObject response = sendHttpsRequest(data, "ocrapi/generalocr");

        return response;
    }

    private String getBase64FromFile(String filePath) throws IOException {
        File imageFile = new File(filePath);
        if (imageFile.exists()) {
            InputStream in = new FileInputStream(imageFile);
            byte data[] = new byte[(int) imageFile.length()]; // 创建合适文件大小的数组
            in.read(data); // 读取文件中的内容到b[]数组
            in.close();

            return Base64Util.encode(data);
        } else {
            throw new FileNotFoundException(filePath + " not exist");
        }
    }

    private JSONObject sendHttpsRequest(JSONObject postData, String method)
            throws NoSuchAlgorithmException, KeyManagementException, IOException {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[]{new YouTu.TrustAnyTrustManager()}, new java.security.SecureRandom());

        String mySign = YoutuSign.getSignature(m_appid, m_secret_id, m_secret_key,
                System.currentTimeMillis() / 1000 + EXPIRED_SECONDS, m_user_id);

        System.setProperty("sun.net.client.defaultConnectTimeout", "30000");
        System.setProperty("sun.net.client.defaultReadTimeout", "30000");

        URL url = new URL(m_end_point + method);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setSSLSocketFactory(sc.getSocketFactory());
        connection.setHostnameVerifier(new YouTu.TrustAnyHostnameVerifier());
        // set header
        connection.setRequestMethod("POST");
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("user-agent", "youtu-java-sdk");
        connection.setRequestProperty("Authorization", mySign.toString());

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Content-Type", "text/json");
        connection.connect();

        // POST请求
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.write(postData.toString().getBytes("utf-8"));
        // 刷新、关闭
        out.flush();
        out.close();

        // 读取响应
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
        String lines;
        StringBuffer responseBuffer = new StringBuffer("");
        while ((lines = reader.readLine()) != null) {
            lines = new String(lines.getBytes());
            responseBuffer.append(lines);
        }

        reader.close();
        // 断开连接
        connection.disconnect();

        return JSON.parseObject(responseBuffer.toString());
    }

    private static class TrustAnyTrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
