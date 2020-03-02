package com.eastrobot.kbs.media.common.util.baidu;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.RequestMetaData;
import com.eastrobot.kbs.media.config.ConvertAsrProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 百度引擎工具类
 *
 * @author yogurt_lei
 * @date 2019-10-23 13:50
 */
@Slf4j
@Component
public class BaiduEngine {
    private static AsrClient asrClient;
    private static RequestMetaData requestMetaData;
    @Resource
    private ConvertAsrProperties convertAsrProperties;

    @PostConstruct
    public void init() {
        ConvertAsrProperties.BaiduBce baiduBce = convertAsrProperties.getBaiduBce();
        AsrConfig asrConfig = AsrConfig.builder()
                .serverIp(baiduBce.getServerIp())
                .serverPort(baiduBce.getServerPort())
                .appName(baiduBce.getAppName())
                .product(baiduBce.getAsrProduct())
                .userName(baiduBce.getUserName())
                .password(baiduBce.getPassword())
                .build();
        asrClient = AsrClientFactory.buildClient(asrConfig);

        requestMetaData = new RequestMetaData();
        requestMetaData.setSendPackageRatio(1);
        requestMetaData.setSleepRatio(1);
        requestMetaData.setTimeoutMinutes(120);
        requestMetaData.setEnableFlushData(true);
    }

    public static AsrClient ofClient() {
        return asrClient;
    }

    public static RequestMetaData ofMetaData() {
        return requestMetaData;
    }
}
