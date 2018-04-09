package com.eastrobot.converter.service.impl;

import com.eastrobot.converter.service.AudioService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AudioServiceImplTest {

    @Autowired
    private AudioService audioService;

    @Test
    public void handle() {
        audioService.handle("D:\\ffmpeg\\bin\\baidu\\16k.pcm");
    }
}