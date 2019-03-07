/*
 * Power by www.xiaoi.com
 */
package com.eastrobot.kbs.media.util.youtu;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.file.Paths;

/**
 * @author <a href="mailto:eko.z@outlook.com">eko.zhan</a>
 * @version 1.0
 * @date 2019/3/7 11:39
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class YouTuOcrUtilTests {

    @Test
    public void testPorn() throws Exception {
        File dir = Paths.get("E:\\Pictures\\2019\\porn-check").toFile();
        for (File file : dir.listFiles()) {
            System.out.println(YouTuOcrUtil.porn(file.getAbsolutePath()));

        }
    }

    @Test
    public void testTerrorism() throws Exception {
        File dir = Paths.get("E:\\Pictures\\2019\\porn-check").toFile();
        for (File file : dir.listFiles()) {
            System.out.println(YouTuOcrUtil.terrorism(file.getAbsolutePath()));

        }
    }
}
