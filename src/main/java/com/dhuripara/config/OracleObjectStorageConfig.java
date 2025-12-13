package com.dhuripara.config;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest;
import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse;
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
    private String configuredNamespace;

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
    public String objectStorageNamespace(ObjectStorage objectStorageClient) {
        // If namespace is explicitly configured and not empty, use it
        if (configuredNamespace != null && !configuredNamespace.trim().isEmpty()) {
            // Validate that it's not an OCID (OCIDs contain colons)
            if (!configuredNamespace.contains(":")) {
                log.info("Using configured namespace: {}", configuredNamespace);
                return configuredNamespace;
            } else {
                log.warn("Configured namespace appears to be an OCID (contains ':'), will retrieve namespace name from API");
            }
        }
        
        // Otherwise, retrieve it from the Object Storage API
        try {
            GetNamespaceRequest getNamespaceRequest = GetNamespaceRequest.builder().build();
            GetNamespaceResponse namespaceResponse = objectStorageClient.getNamespace(getNamespaceRequest);
            String namespace = namespaceResponse.getValue();
            log.info("Retrieved namespace from Object Storage API: {}", namespace);
            return namespace;
        } catch (Exception e) {
            log.error("Failed to retrieve namespace from Object Storage API", e);
            throw new RuntimeException("Failed to retrieve namespace from Object Storage API. Please configure oracle.objectstorage.namespace with the namespace name (not OCID)", e);
        }
    }

    @Bean
    public String objectStorageBucketName() {
        return bucketName;
    }
}

