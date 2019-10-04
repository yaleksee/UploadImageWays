package com.upload.sbfileupload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by alekseenkoyuri1989@gmail.com on 01.10.2019
 */
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
