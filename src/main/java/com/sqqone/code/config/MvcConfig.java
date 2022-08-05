package com.sqqone.code.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/7/1 10:59
 */
//@Configuration
public class MvcConfig extends WebMvcConfigurationSupport {

    @Value("${imgFilepPath}")
    private String imgFilepPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/META-INF/resources/", "classpath:/static/");
//        registry.addResourceHandler("/static/img/**").addResourceLocations("file:/" +imgFilepPath);
        registry.addResourceHandler("/static/img/**").addResourceLocations("file:" +imgFilepPath);
        super.addResourceHandlers(registry);
    }
}
