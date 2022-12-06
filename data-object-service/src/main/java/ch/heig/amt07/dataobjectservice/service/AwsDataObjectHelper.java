package ch.heig.amt07.dataobjectservice.service;

import ch.heig.amt07.dataobjectservice.utils.AwsConfigProvider;
import ch.heig.amt07.dataobjectservice.utils.exceptions.ObjectAlreadyExistsException;
import ch.heig.amt07.dataobjectservice.utils.exceptions.ObjectNotFoundException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AwsDataObjectHelper {

    private final AwsConfigProvider configProvider;
    private static final Logger LOG = Logger.getLogger(AwsDataObjectHelper.class.getName());
    private final S3Client s3;
    private final String rootObjectName;

    public AwsDataObjectHelper(AwsConfigProvider configProvider, String rootObjectName) {
        this.configProvider = configProvider;
        this.rootObjectName = rootObjectName;
        s3 = S3Client.builder()
                .region(configProvider.getRegion())
                .credentialsProvider(configProvider.getCredentialsProvider())
                .build();
    }

    public boolean existsRootObject(String rootObjectName) {
        HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(rootObjectName)
                .build();
        try {
            s3.headBucket(headBucketRequest);
            return true;
        } catch (NoSuchBucketException e) {
            LOG.log(Level.INFO, "{0}", e.getMessage());
            return false;
        }
    }

    public void createRootObject(String rootObjectName) {
        if (!existsRootObject(rootObjectName)) {
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(rootObjectName)
                    .build();
            s3.createBucket(createBucketRequest);
        }
    }

    public void removeRootObject(String rootObjectName) {
        if (existsRootObject(rootObjectName)) {
            DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder()
                    .bucket(rootObjectName)
                    .build();
            s3.deleteBucket(deleteBucketRequest);
        }
    }

    public void createObject(String objectName, Path filePath) {
        if (existsObject(objectName)) {
            LOG.log(Level.INFO, "Object {0} already exists", objectName);
            throw new ObjectAlreadyExistsException(objectName + " already exists");
        }
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(rootObjectName)
                .key(objectName)
                .build();
        s3.putObject(objectRequest, RequestBody.fromFile(filePath));
    }

    public boolean existsObject(String objectName) {
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(rootObjectName)
                .key(objectName)
                .build();
        try {
            s3.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            LOG.log(Level.INFO, "{0}", e.getMessage());
            return false;
        }
    }

    public void removeObject(String objectName) {
        if (!existsObject(objectName)) {
            throw new ObjectNotFoundException(objectName + " does not exist");
        }
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(rootObjectName)
                .key(objectName)
                .build();
        s3.deleteObject(deleteObjectRequest);
    }

    public boolean downloadObject(String objectUrl, Path downloadedImagePath) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(rootObjectName)
                .key(objectUrl)
                .build();
        try {
            s3.getObject(getObjectRequest, downloadedImagePath);
            return true;
        } catch (NoSuchKeyException e) {
            throw new ObjectNotFoundException("Object " + objectUrl + " not found");
        }
    }

    public String getPresignedUrl(String objectName, long expirationTimeinSeconds) {
        if (expirationTimeinSeconds <= 0) {
            throw new IllegalArgumentException("Expiration time must be greater than 0");
        }

        if (!existsObject(objectName)) {
            throw new ObjectNotFoundException("Object " + objectName + " not found");
        }

        try (S3Presigner presigner = S3Presigner.builder()
                .credentialsProvider(configProvider.getCredentialsProvider())
                .region(configProvider.getRegion())
                .build()) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(rootObjectName)
                    .key(objectName)
                    .build();
            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(java.time.Duration.ofSeconds(expirationTimeinSeconds))
                    .getObjectRequest(getObjectRequest)
                    .build();
            return presigner.presignGetObject(getObjectPresignRequest).url().toString();
        }
    }
}
