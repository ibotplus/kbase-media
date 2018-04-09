package com.eastrobot.converter.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * PropertiesUtil
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-02 18:01
 */
@Slf4j
public class PropertiesUtil {
    private static Map<String, String> propertiesMap = new HashMap<>();

    private static void processProperties(Properties props) throws BeansException {
        propertiesMap = new HashMap<>();
        for (Object key : props.keySet()) {
            String keyStr = key.toString();
            try {
                // PropertiesLoaderUtils的默认编码是ISO-8859-1,在这里转码一下
                propertiesMap.put(keyStr, new String(props.getProperty(keyStr).getBytes("ISO-8859-1"), "utf-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadAllProperties(String propertyFileName) {
        try {
            Properties properties = PropertiesLoaderUtils.loadAllProperties(propertyFileName);
            log.info(properties.toString());
            processProperties(properties);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getString(String key) {
        return getString(key, "");
    }

    public static String getString(String key, String defaultValue){
        String val = propertiesMap.get(key);
        return StringUtils.isBlank(val) || val.startsWith("#") ? defaultValue : val;
    }

    public static String value(String key){
        return getString(key, null);
    }

    public static boolean getBoolean(String key){
        return getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defaultValue){
        return StringUtils.isNotBlank(value(key)) ? Boolean.valueOf(value(key)) : defaultValue;
    }
}
