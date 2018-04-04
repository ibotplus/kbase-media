package com.eastrobot.converter.config;

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
    private static final String VCA = "vca";
    private static final String ASR = "asr";
    private static final String OCR = "ocr";
    private static final String DEFAULT = "default";
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
        Map videoConfig = (Map) video.get(VCA);
        String videoTool = (String) videoConfig.get(DEFAULT);

        return (Map) videoConfig.get(videoTool);
    }

    public Map getDefaultAudioConfig() {
        Map audioConfig = (Map) audio.get(ASR);
        String audioTool = (String) audioConfig.get(DEFAULT);

        return (Map) audioConfig.get(audioTool);    }

    public Map getDefaultImageConfig() {
        Map imageConfig = (Map) image.get(OCR);
        String imageTool = (String) imageConfig.get(DEFAULT);

        return (Map) imageConfig.get(imageTool);    }
}
