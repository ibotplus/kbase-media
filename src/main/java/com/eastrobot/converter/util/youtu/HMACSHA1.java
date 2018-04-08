package com.eastrobot.converter.util.youtu;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * HMACSHA1
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-08 15:13
 */
public class HMACSHA1 {
    public static byte[] getSignature(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), mac.getAlgorithm());
        mac.init(signingKey);

        return mac.doFinal(data.getBytes());
    }
}
