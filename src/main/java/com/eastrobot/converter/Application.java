package com.eastrobot.converter;

import com.eastrobot.converter.web.listener.PropertiesListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        // 注册监听器
        application.addListeners(new PropertiesListener("application.properties"));
        application.run(args);
    }
}