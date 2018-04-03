package com.eastrobot.converter.web.listener;

import com.eastrobot.converter.util.PropertiesUtil;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

/**
 * PropertiesListener
 *
 * @author <a href="yogurt.lei@xiaoi.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-02 17:57
 */
public class PropertiesListener implements ApplicationListener<ApplicationStartedEvent> {
    private String propertiesFile;

    public PropertiesListener(String propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        PropertiesUtil.loadAllProperties(propertiesFile);
    }
}
