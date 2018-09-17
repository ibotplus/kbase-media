package com.eastrobot.kbs.media.util.youtu;

import java.util.Random;

/**
 * YoutuSign 签名方法Sign = Base64(HMAC-SHA1(SecretKey, orignal) + original)
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-08 15:11
 */
public class YoutuSign {
    public static String getSignature(String appId, String secret_id,
                                      String secret_key, long expired, String userid) {
        /**
         *  u为开发者创建应用时的QQ号
         *      a为用户的AppID
         *      k为用户的SecretID
         *      t为当前时间戳，是一个符合UNIX Epoch时间戳规范的数值，单位为秒
         *      e为此签名的凭证有效期，是一个符合UNIX Epoch时间戳规范的数值，单位为秒,
         *         e应大于t, 生成的签名在 t 到 e 的时间内 都是有效的. 如果是0, 则生成的签名只有再t的时刻是有效的.
         *      r为随机串，无符号10进制整数，用户需自行生成，最长10位。
         *      f为空
         *      拼接有效签名串的结果,下文称之为orignal
         */
        long now = System.currentTimeMillis() / 1000;
        int rdm = Math.abs(new Random().nextInt());
        String orignal = "a=" + appId + "&k=" + secret_id + "&e=" + expired + "&t=" + now + "&r=" + rdm + "&u=" +
                userid;

        byte[] bin = hashHmac(orignal, secret_key);

        byte[] all = new byte[bin.length + orignal.getBytes().length];
        System.arraycopy(bin, 0, all, 0, bin.length);
        System.arraycopy(orignal.getBytes(), 0, all, bin.length, orignal.getBytes().length);

        return Base64Util.encode(all);
    }

    private static byte[] hashHmac(String plain_text, String accessKey) {
        try {
            return HMACSHA1.getSignature(plain_text, accessKey);
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}
