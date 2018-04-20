package com.eastrobot.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Slf4j
@SpringBootApplication(exclude = {MultipartAutoConfiguration.class})
@EnableConfigurationProperties
public class Application {
    public static void main(String[] args) {
        new SpringApplication(Application.class).run(args);
    }

    /**
     * 替换默认文件上传实现StandardServletMultipartResolver
     */
    @Bean("multipartResolver")
    public CommonsMultipartResolver multipartResolver() {
        return new CommonsMultipartResolver();
    }
}