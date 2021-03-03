package com.second_hand_trading_platform.second_hand_trading_platform.config;

import com.second_hand_trading_platform.second_hand_trading_platform.utils.FilePathProvider;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Configuration
public class WebMvcCfg implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String imagePath ="file:"+ FilePathProvider.getImgPath();
        log.debug("Image File path: { " + imagePath+" }");
        registry.addResourceHandler("/img/**").addResourceLocations(imagePath);
    }



    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer(){
        return new WebServerFactoryCustomizer<ConfigurableWebServerFactory>() {
            @Override
            public void customize(ConfigurableWebServerFactory factory) {
                factory.setPort(8080);
                ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/index.html");

                factory.addErrorPages(error404Page);
            }
        };
    }
}
