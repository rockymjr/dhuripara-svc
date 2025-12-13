package com.dhuripara.service;

import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.requests.*;
import com.oracle.bmc.objectstorage.responses.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class OracleObjectStorageService {

    private final ObjectStorage objectStorageClient;
    private final String objectStorageNamespace;
    private final String objectStorageBucketName;
    
    public OracleObjectStorageService(
            ObjectStorage objectStorageClient,
            @Qualifier("objectStorageNamespace") String objectStorageNamespace,
            @Qualifier("objectStorageBucketName") String objectStorageBucketName) {
        this.objectStorageClient = objectStorageClient;
        this.objectStorageNamespace = objectStorageNamespace;
        this.objectStorageBucketName = objectStorageBucketName;
    }
    
    // Use the namespace and bucket name from beans
    private String getNamespace() {
        return objectStorageNamespace;
    }
    
    private String getBucketName() {
        return objectStorageBucketName;
    }

    public String uploadFile(MultipartFile file, String folderPath) throws Exception {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String objectName = folderPath != null && !folderPath.isEmpty() 
            ? folderPath + "/" + fileName 
            : fileName;

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .namespaceName(getNamespace())
                    .bucketName(getBucketName())
                    .putObjectBody(inputStream)
                    .objectName(objectName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            objectStorageClient.putObject(putObjectRequest);
            log.info("File uploaded successfully: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("Failed to upload file to Oracle Object Storage", e);
            throw new Exception("Failed to upload file: " + e.getMessage(), e);
        }
    }

    public InputStream downloadFile(String objectName) throws Exception {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .namespaceName(getNamespace())
                    .bucketName(getBucketName())
                    .objectName(objectName)
                    .build();

            GetObjectResponse getObjectResponse = objectStorageClient.getObject(getObjectRequest);
            return getObjectResponse.getInputStream();
        } catch (Exception e) {
            log.error("Failed to download file from Oracle Object Storage: {}", objectName, e);
            throw new Exception("Failed to download file: " + e.getMessage(), e);
        }
    }

    public String generatePreSignedUrl(String objectName, int expirationHours) throws Exception {
        try {
            String accessUri = "accessUri_" + UUID.randomUUID().toString();
            
            Date expirationDate = new Date(System.currentTimeMillis() + (expirationHours * 60 * 60 * 1000L));
            
            CreatePreauthenticatedRequestDetails details = CreatePreauthenticatedRequestDetails.builder()
                    .name(accessUri)
                    .objectName(objectName)
                    .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectRead)
                    .timeExpires(expirationDate)
                    .build();

            CreatePreauthenticatedRequestRequest request = CreatePreauthenticatedRequestRequest.builder()
                    .namespaceName(getNamespace())
                    .bucketName(getBucketName())
                    .createPreauthenticatedRequestDetails(details)
                    .build();

            CreatePreauthenticatedRequestResponse response = 
                    objectStorageClient.createPreauthenticatedRequest(request);

            String preSignedUrl = response.getPreauthenticatedRequest().getAccessUri();
            log.info("Generated pre-signed URL for: {}", objectName);
            return preSignedUrl;
        } catch (Exception e) {
            log.error("Failed to generate pre-signed URL for: {}", objectName, e);
            throw new Exception("Failed to generate pre-signed URL: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String objectName) throws Exception {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .namespaceName(getNamespace())
                    .bucketName(getBucketName())
                    .objectName(objectName)
                    .build();

            objectStorageClient.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully: {}", objectName);
        } catch (Exception e) {
            log.error("Failed to delete file from Oracle Object Storage: {}", objectName, e);
            throw new Exception("Failed to delete file: " + e.getMessage(), e);
        }
    }
}

