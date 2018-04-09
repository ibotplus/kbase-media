package com.eastrobot.converter.util.ffmpeg;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FFmpegUtilTest {

    @Test
    public void splitAudio() throws Exception {
        FFmpegUtil.splitBaiduAsrAudio("D:\\ffmpeg\\bin\\1111.Wmv",40, "sadasd");
    }
}