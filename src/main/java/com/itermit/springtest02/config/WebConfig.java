package com.itermit.springtest02.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.time.Duration;

//@Configuration
//@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/articles/**")
//                .addResourceLocations("/public", "classpath:/static/")
                .addResourceLocations("classpath:/public/images/articles/")
//                .addResourceLocations("file://" + System.getProperty("user.dir") + "/Projects/Java/spring-test02/src/main/resources/public/images/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)))
//                .setCachePeriod(0);
//                .setCachePeriod(0)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }
}