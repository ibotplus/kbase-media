package com.eastrobot.kbs.media.config;

import com.baidu.acu.pie.model.AsrProduct;
import com.eastrobot.kbs.media.common.util.asr.AsrEngine;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2019-04-22 10:03
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties("convert.audio.asr")
public class ConvertAsrProperties {

    /**
     * asr 默认引擎
     */
    @Value("${convert.audio.asr.default}")
    String defaultAsrEngine;
    /**
     * asr 默认引擎
     */
    AsrEngine asrEngine = AsrEngine.ALI_NLS;

    /**
     * 百度bce配置
     */
    private BaiduBce baiduBce = new BaiduBce();

    /**
     * 阿里nls配置
     */
    private AliNls aliNls = new AliNls();

    @PostConstruct
    private void valid() {
        if (AsrEngine.BAIDU_BCE.name().equals(defaultAsrEngine)){
            asrEngine = AsrEngine.BAIDU_BCE;
        }
        log.info("当前选择的asr引擎是: [{}]", asrEngine);
    }

    @Data
    public static class BaiduBce {
        /**
         * asr流式服务器地址
         */
        private String serverIp = "asr.baiduai.cloud";

        /**
         * asr流式服务的端口
         */
        private int serverPort = 8051;
        /**
         * asr客户端的名称
         */
        private String appName = "kbase-trainer";
        /**
         * 用户名
         */
        private String userName = "zhangkazhongxing";
        /**
         * 密码
         */
        private String password = "zhangkazhongxing";
        /**
         * 通讯模型 注意采样率
         */
        private AsrProduct asrProduct = AsrProduct.FAR_FIELD;
    }

    @Data
    public static class AliNls {
        /**
         * 阿里服务地址
         */
        private String serverIp = "ws://172.16.7.51:8101/ws/v1";
        /**
         * 阿里授权appKey
         */
        private String appKey = "default";

        /**
         * 阿里授权accessToken
         */
        private String accessToken = "default";
        /**
         * 阿里引擎自定义参数，调用模板id
         */
        private String customizationId = "";
    }
}
