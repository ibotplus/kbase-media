package com.eastrobot.converter.util;

import java.util.Properties;

public class SystemUtils {
    private static final String UTF8 = "UTF-8";
    private static final String OS_LINUX = "LINUX";

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
}
