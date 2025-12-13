package com.dhuripara.config;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
public class OracleObjectStorageConfig {

    @Value("${oracle.objectstorage.config-file-path:${user.home}/.oci/config}")
    private String configFilePath;

    @Value("${oracle.objectstorage.profile:DEFAULT}")
    private String profile;

    @Value("${oracle.objectstorage.namespace:}")
    private String namespace;

    @Value("${oracle.objectstorage.bucket-name:dhuripara-documents}")
    private String bucketName;

    @Bean
    public ObjectStorage objectStorageClient() {
        try {
            ConfigFileReader.ConfigFile configFile = ConfigFileReader.parse(configFilePath, profile);
            AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);
            return new ObjectStorageClient(provider);
        } catch (IOException e) {
            log.error("Failed to initialize Oracle Object Storage client", e);
            throw new RuntimeException("Failed to initialize Oracle Object Storage client", e);
        }
    }

    @Bean
    public String objectStorageNamespace() {
        return namespace;
    }

    @Bean
    public String objectStorageBucketName() {
        return bucketName;
    }
}

