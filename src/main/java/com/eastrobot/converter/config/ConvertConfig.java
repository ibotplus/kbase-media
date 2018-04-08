package com.eastrobot.converter.config;

import com.eastrobot.converter.model.Constants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 加载自定义Converter yml配置
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-04 23:53
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "convert")
public class ConvertConfig {
    /**
     * 默认转换输出路径
     */
    private String outputFolder;
    /**
     * 视频转换工具全局配置
     */
    private Map video = new HashMap<>();
    /**
     * 音频转换工具全局配置
     */
    private Map audio = new HashMap<>();
    /**
     * 图片转换工具全局配置
     */
    private Map image = new HashMap<>();

    public Map getDefaultVideoConfig() {
        return (Map) video.get(Constants.VCA);
    }

    public Map getDefaultAudioConfig() {
        return (Map) audio.get(Constants.ASR);
    }

    public Map getDefaultImageConfig() {
        return (Map) image.get(Constants.OCR);
    }
}
