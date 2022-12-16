package ch.heig.amt07.dataobjectservice.service;

import ch.heig.amt07.dataobjectservice.exception.NotEmptyException;
import ch.heig.amt07.dataobjectservice.exception.ObjectAlreadyExistsException;
import ch.heig.amt07.dataobjectservice.exception.ObjectNotFoundException;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AwsDataObjectService {

    public static final long MIN_EXPIRATION_IN_SECONDS = 90;
    private static final Logger LOG = Logger.getLogger(AwsDataObjectService.class.getName());

    private final AwsCredentialsProvider credentialsProvider;
    private final Region region;
    private final S3Client s3;
    private final String rootObjectName;

    public AwsDataObjectService() {
        this("AWS_ACCESS_KEY_ID", "AWS_SECRET_ACCESS_KEY", "AWS_DEFAULT_REGION", "AWS_BUCKET");
    }

    public AwsDataObjectService(String accessKeyVar, String secretKeyVar, String regionVar, String rootObjectNameVar) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .systemProperties()
                .load();
        AwsBasicCredentials credentials = AwsBasicCredentials.create(dotenv.get(accessKeyVar), dotenv.get(secretKeyVar));
        credentialsProvider = StaticCredentialsProvider.create(credentials);
        region = Region.of(dotenv.get(regionVar));

        this.rootObjectName = dotenv.get(rootObjectNameVar);
        s3 = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    public boolean existsRootObject(String rootObjectName) {
        HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(rootObjectName)
                .build();
        try {
            s3.headBucket(headBucketRequest);
            return true;
        } catch (S3Exception e) {
            LOG.log(Level.INFO, "{0}", e.getMessage());
            return false;
        }
    }

    public void createRootObject(String rootObjectName) {
        if (existsRootObject(rootObjectName)) {
            throw new ObjectAlreadyExistsException(rootObjectName + " already exists");
        }
        CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                .bucket(rootObjectName)
                .build();
        s3.createBucket(createBucketRequest);
    }

    public void removeRootObject(String rootObjectName, boolean recursive) {
        if (!existsRootObject(rootObjectName)) {
            throw new ObjectNotFoundException(rootObjectName + " does not exist");
        }

        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(rootObjectName)
                .build();
        ListObjectsV2Response listObjectsV2Response = s3.listObjectsV2(listObjectsV2Request);

        if (recursive) {
            do {
                listObjectsV2Response = s3.listObjectsV2(listObjectsV2Request);
                for (S3Object s3Object : listObjectsV2Response.contents()) {
                    DeleteObjectRequest request = DeleteObjectRequest.builder()
                            .bucket(rootObjectName)
                            .key(s3Object.key())
                            .build();
                    s3.deleteObject(request);
                }
            } while (listObjectsV2Response.isTruncated());

            DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(rootObjectName).build();
            s3.deleteBucket(deleteBucketRequest);
        } else {
            if (!listObjectsV2Response.contents().isEmpty()) {
                throw new NotEmptyException(rootObjectName + " is not empty");
            }

            DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder()
                    .bucket(rootObjectName)
                    .build();
            s3.deleteBucket(deleteBucketRequest);
        }
    }

    public boolean existsObject(String objectName){
        if (objectName.endsWith("/")) {
            return existsFolderObject(objectName);
        } else {
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
    }

    private boolean existsFolderObject(String objectName) {
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(rootObjectName)
                .prefix(objectName)
                .build();
        ListObjectsV2Response listObjectsV2Response = s3.listObjectsV2(listObjectsV2Request);
        return !listObjectsV2Response.contents().isEmpty();
    }

    public void createObject(String objectName, byte[] file) {
        if (!existsRootObject(rootObjectName)) {
            createRootObject(rootObjectName);
        }

        if (existsObject(objectName)) {
            throw new ObjectAlreadyExistsException(objectName + " already exists");
        }
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(rootObjectName)
                .key(objectName)
                .build();
        s3.putObject(objectRequest, RequestBody.fromBytes(file));
    }

    public void removeObject(String objectName, Boolean recursive) {
        if (!existsObject(objectName)) {
            throw new ObjectNotFoundException(objectName + " does not exist");
        }
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(rootObjectName)
                .prefix(objectName)
                .build();
        ListObjectsV2Response listObjectsV2Response = s3.listObjectsV2(listObjectsV2Request);

        if (recursive) {
            do {
                listObjectsV2Response = s3.listObjectsV2(listObjectsV2Request);
                for (S3Object s3Object : listObjectsV2Response.contents()) {
                    DeleteObjectRequest request = DeleteObjectRequest.builder()
                            .bucket(rootObjectName)
                            .key(s3Object.key())
                            .build();
                    s3.deleteObject(request);
                }
            } while (listObjectsV2Response.isTruncated());
        } else {
            if (objectName.endsWith("/") && !listObjectsV2Response.contents().isEmpty()) {
                throw new NotEmptyException(rootObjectName + " is not empty");
            }
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(rootObjectName)
                    .key(objectName)
                    .build();
            s3.deleteObject(deleteObjectRequest);
        }
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

    public String getPresignedUrl(String objectName, Optional<Long> expirationTimeinSeconds) {
        long expiration = expirationTimeinSeconds.orElse(MIN_EXPIRATION_IN_SECONDS);
        if (expiration <= 0) {
            throw new IllegalArgumentException("Expiration time must be greater than 0");
        }

        if (!existsObject(objectName)) {
            throw new ObjectNotFoundException("Object " + objectName + " not found");
        }

        try (S3Presigner presigner = S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build()) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(rootObjectName)
                    .key(objectName)
                    .build();
            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(java.time.Duration.ofSeconds(expiration))
                    .getObjectRequest(getObjectRequest)
                    .build();
            return presigner.presignGetObject(getObjectPresignRequest).url().toString();
        }
    }
}
