package com.eastrobot.converter.util.shhan;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.apache.commons.io.IOUtils.copy;

@Slf4j
@Component
public class ShhanUtil {

    @Value("${convert.audio.asr.shhan.base-url}")
    private String shhanBaseUrl;

    private static ShhanUtil shhanUtil;

    @PostConstruct
    public void init() {
        shhanUtil = this;
    }

	public static String asr(String filePath) {
        String result;
        try {
            byte[] data = FileUtils.readFileToByteArray(new File(filePath));
            result = rec(data, shhanUtil.shhanBaseUrl + "recogBatch");

            result = StringUtils.substringAfter(result, "<s>");
            return result.replaceAll("\\s*|\t|\n", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String rec(byte[] data, String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.addRequestProperty("Content-Type", "audio/pcm");
        conn.setRequestProperty("length", String.valueOf(data.length));
        conn.setRequestProperty("sampleRate", "16000");
         OutputStream out = conn.getOutputStream();
        out.write(data);
        out.flush();
        out.close();
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        InputStream in = conn.getInputStream();
        copy(in, bytesOut);
        in.close();
        byte[] txtBytes = bytesOut.toByteArray();
        return new String(txtBytes, "utf-8");
    }
}
 