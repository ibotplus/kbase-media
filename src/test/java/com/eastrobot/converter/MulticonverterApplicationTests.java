package com.eastrobot.converter;

import com.eastrobot.converter.config.ConvertConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MulticonverterApplicationTests {

    @Autowired
    private ConvertConfig convertConfig;

    @Autowired
    private Environment env;

    @Test
    public void contextLoads() {
    }

    @Test
    public void configTest() {
        System.out.println(convertConfig.getOutputFolder());
        System.out.println(convertConfig.getVideo());
        System.out.println(convertConfig.getAudio());
        System.out.println(convertConfig.getImage());

        System.out.println(convertConfig.getDefaultVideoConfig());
        System.out.println(convertConfig.getDefaultAudioConfig());
        System.out.println(convertConfig.getDefaultImageConfig());

        System.out.println(env.getProperty("developer"));
    }
}
