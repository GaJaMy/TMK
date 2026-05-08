package com.tmk.api.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.document")
public class FileStorageProperties {

    private String storageDir = "./tmk-api/uploads";
}
