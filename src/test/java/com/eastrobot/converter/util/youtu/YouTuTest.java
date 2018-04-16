package com.eastrobot.converter.util.youtu;

import com.eastrobot.converter.service.YouTuService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class YouTuTest {

    @Autowired
    private YouTuService youTuService;

    @Test
    public void generalOcr() {
        String ocr = null;
        try {
            ocr = youTuService.ocr("C:\\Users\\User\\Desktop\\121.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(ocr);
    }
}