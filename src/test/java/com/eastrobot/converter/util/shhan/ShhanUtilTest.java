package com.eastrobot.converter.util.shhan;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ShhanUtilTest {

    @Test
    public void asr() {
        ShhanUtil.asr("D:\\ffmpeg\\bin\\baidu\\16k.pcm");
    }
}