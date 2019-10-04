package com.upload.sbfileupload;

import com.upload.sbfileupload.config.ApplicationShutdown;
import com.upload.sbfileupload.config.StorageProperties;
import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;

/**
 * Created by alekseenkoyuri1989@gmail.com on 01.10.2019
 */
@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class SpringBootFileUploadApplication {

    public static void main(String[] args) {
        BasicConfigurator.configure();
        SpringApplication.run(SpringBootFileUploadApplication.class, args);
    }

    @Bean
    public ApplicationShutdown applicationShutdown() {
        return new ApplicationShutdown();
    }

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory(final ApplicationShutdown applicationShutdown) {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(applicationShutdown);
        return factory;
    }
}
