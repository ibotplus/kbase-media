package com.eastrobot.converter.plugin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * AsyncMode 异步模式是否启动
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-19 15:25
 */
@Slf4j
@ConditionalOnProperty(prefix = "convert", name = "enable-async", havingValue = "true")
@Component
public class AsyncMode {

    @PostConstruct
    private void init() {
        log.warn("AsyncMode is started.");
    }
}
