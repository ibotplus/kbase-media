package com.eastrobot.converter.util;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

public class SystemUtils {
    public static final String UTF8 = "UTF-8";
    public static final String OS_LINUX = "LINUX";

    /**
     * 判断当前操作系统是 linux or windows
     *
     * @author eko.zhan at 2017年12月19日 下午6:20:54
     */
    public static boolean isLinux() {
        Properties prop = System.getProperties();
        String defaultOS = prop.getProperty("os.name").toUpperCase();
        return defaultOS.contains(OS_LINUX);
    }

    public static String RealPath() {
        String realPath = "";
        try {
            File file = ResourceUtils.getFile("classpath:application.yml");
            realPath = file.getParent().replaceAll("[/\\\\]{1}WEB-INF[/\\\\]{1}classes[/\\\\]?", "").replace("%20", "" +
                    " ");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return realPath;
    }
}
