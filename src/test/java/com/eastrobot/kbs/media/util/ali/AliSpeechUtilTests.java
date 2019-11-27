/*
 * Powered by http://ibotstat.com
 */
package com.eastrobot.kbs.media.util.ali;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:eko.z@outlook.com">eko.zhan</a>
 * @version v1.0
 * @date 2019/11/27 17:31
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AliSpeechUtilTests {

    @Test
    public void testAsr(){
        try {
            String result = AliSpeechUtil.asr("E:\\AI\\ASR\\02.wav");
            System.out.println(result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
