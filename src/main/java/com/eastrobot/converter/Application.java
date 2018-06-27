package com.eastrobot.converter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;

@EnableConfigurationProperties
@SpringBootApplication(exclude = {MultipartAutoConfiguration.class})
public class Application {
    public static void main(String[] args) {
        new SpringApplication(Application.class).run(args);
    }

    /**
     * 替换默认文件上传实现StandardServletMultipartResolver
     */
    @Bean(DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        // 上传不受限 由逻辑控制
        resolver.setMaxInMemorySize(-1);
        resolver.setMaxUploadSizePerFile(-1);

        return resolver;
    }
}