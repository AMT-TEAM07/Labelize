package ch.heig.amt07.dataobjectservice;

import com.sun.tools.javac.Main;
import io.github.cdimascio.dotenv.Dotenv;
import ch.heig.amt07.dataobjectservice.service.AwsDataObjectHelper;
import ch.heig.amt07.dataobjectservice.utils.AwsConfigProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DataObjectServiceApplicationTests {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    private AwsDataObjectHelper bucketManager;
    private String bucketName;
    private Path testImagePath;
    private Path downloadedImagePath;
    private String objectName;

    @BeforeEach
    public void setup() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .systemProperties()
                .load();

        bucketName = dotenv.get("TEST_AWS_BUCKET");
        objectName = "test-image.png";
        testImagePath = Paths.get("src", "test", "resources", objectName);
        downloadedImagePath = Paths.get("src", "test", "resources", "downloaded-" + objectName);

        var configProvider = new AwsConfigProvider("TEST_AWS_ACCESS_KEY_ID", "TEST_AWS_SECRET_ACCESS_KEY", "TEST_AWS_DEFAULT_REGION");
        bucketManager = new AwsDataObjectHelper(configProvider, bucketName);
    }

    @Test
    void canCreateObjectInExistingRootObject() {
        //given
        assertTrue(bucketManager.existsRootObject(bucketName));
        assertFalse(bucketManager.existsObject(objectName));

        //when
        bucketManager.createObject(objectName, testImagePath);

        //then
        assertTrue(bucketManager.existsObject(objectName));
    }

    @Test
    void canConfirmRootObjectExists() {
        //given
        String existingBucket = bucketName;
        boolean actualResult;

        //when
        actualResult = bucketManager.existsRootObject(existingBucket);

        //then
        assertTrue(actualResult);
    }

    @Test
    void canConfirmRootObjectDoesNotExist() {
        //given
        String notExistingBucket = "notExistingBucket-" + bucketName;
        boolean actualResult;

        //when
        actualResult = bucketManager.existsRootObject(notExistingBucket);

        //then
        assertFalse(actualResult);
    }

    @Test
    void canConfirmObjectExists() {
        //given
        bucketManager.createObject(objectName, testImagePath);
        boolean actualResult;

        //when
        actualResult = bucketManager.existsObject(objectName);

        //then
        assertTrue(actualResult);
    }

    @Test
    void canConfirmObjectDoesNotExist() {
        //given
        String notExistingFileName = "notExistingFile.jpg";
        assertTrue(bucketManager.existsRootObject(bucketName));
        boolean actualResult;

        //when
        actualResult = bucketManager.existsObject(notExistingFileName);

        //then
        assertFalse(actualResult);
    }

    @Test
    void canRemoveObjectFromNotEmptyRootObject() {
        //given
        assertTrue(bucketManager.existsRootObject(bucketName));
        bucketManager.createObject(objectName, testImagePath);
        assertTrue(bucketManager.existsObject(objectName));

        //when
        bucketManager.removeObject(objectName);

        //then
        assertFalse(bucketManager.existsObject(objectName));
    }

    @Test
    void canDownloadExistingObject() {
        //given
        bucketManager.createObject(objectName, testImagePath);
        assertTrue(bucketManager.existsObject(objectName));
        boolean actualResult;

        //when
        actualResult = bucketManager.downloadObject(objectName, downloadedImagePath);

        //then
        assertTrue(actualResult);
        File file = new File(downloadedImagePath.toUri());
        assertTrue(file.exists());
        assertEquals(file.length(), testImagePath.toFile().length());
    }

    @Test
    void returnFalseWhenDownloadingNotExistingObject() {
        //given
        assertFalse(bucketManager.existsObject(objectName));
        boolean actualResult;

        //when
        actualResult = bucketManager.downloadObject(objectName, downloadedImagePath);

        //then
        assertFalse(actualResult);
        File file = new File(downloadedImagePath.toUri());
        assertFalse(file.exists());
    }

    @Test
    void canGeneratePresignedPublicUrlForGivenObjectName() {
        //given
        bucketManager.createObject(objectName, testImagePath);
        assertTrue(bucketManager.existsObject(objectName));
        String actualResult;

        //when
        actualResult = bucketManager.getPresignedUrl(objectName);

        //then
        assertNotNull(actualResult);
    }

    @AfterEach
    void tearDown() {
        File file = new File(downloadedImagePath.toUri());
        if (file.exists()) {
            LOG.log(Level.INFO, "{0}", "Deleting file => " + file.delete());
        }
        if (bucketManager.existsObject(objectName)) {
            bucketManager.removeObject(objectName);
        }
    }

}
