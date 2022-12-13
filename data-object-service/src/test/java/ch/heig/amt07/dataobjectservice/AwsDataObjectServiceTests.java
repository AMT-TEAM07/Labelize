package ch.heig.amt07.dataobjectservice;

import ch.heig.amt07.dataobjectservice.exception.NotEmptyException;
import ch.heig.amt07.dataobjectservice.exception.ObjectAlreadyExistsException;
import ch.heig.amt07.dataobjectservice.exception.ObjectNotFoundException;
import ch.heig.amt07.dataobjectservice.service.AwsDataObjectService;
import ch.heig.amt07.dataobjectservice.utils.AwsConfigProvider;
import com.sun.tools.javac.Main;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AwsDataObjectServiceTests {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    private AwsDataObjectService dataObjectService;
    private String rootObjectName;
    private byte[] testFile;
    private Path downloadedImagePath;
    private String folderName;
    private String objectName;

    @BeforeEach
    public void setup() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .systemProperties()
                .load();

        rootObjectName = dotenv.get("TEST_AWS_BUCKET");
        folderName = "test-folder/";
        objectName = "test-image.png";
        Path testImagePath = Paths.get("src", "test", "resources", objectName);

        testFile = new byte[0];
        try {
            testFile = Files.readAllBytes(testImagePath);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }

        downloadedImagePath = Paths.get("src", "test", "resources", "downloaded-" + objectName);

        var configProvider = new AwsConfigProvider("TEST_AWS_ACCESS_KEY_ID", "TEST_AWS_SECRET_ACCESS_KEY", "TEST_AWS_DEFAULT_REGION");
        dataObjectService = new AwsDataObjectService(configProvider, rootObjectName);
    }

    // DoesExist

    @Test
    void DoesObjectExist_RootObjectExists_Exists() {
        //given
        var existingRootObjectName = rootObjectName;
        boolean rootObjectExists;

        //when
        rootObjectExists = dataObjectService.existsRootObject(existingRootObjectName);

        //then
        assertTrue(rootObjectExists);
    }

    @Test
    void DoesObjectExist_RootObjectDoesntExist_DoesntExist() {
        //given
        var notExistingRootObjectName = "notExistingRootObjectName" + rootObjectName;
        boolean rootObjectExists;

        //when
        rootObjectExists = dataObjectService.existsRootObject(notExistingRootObjectName);

        //then
        assertFalse(rootObjectExists);
    }

    @Test
    void DoesObjectExist_RootObjectAndObjectExist_Exists() {
        //given
        assertTrue(dataObjectService.existsRootObject(rootObjectName));
        dataObjectService.createObject(objectName, testFile);
        boolean objectExists;

        //when
        objectExists = dataObjectService.existsObject(objectName);

        //then
        assertTrue(objectExists);
    }

    @Test
    void DoesObjectExist_RootObjectExistObjectDoesntExist_DoesntExist() {
        //given
        String notExistingFileName = "notExistingFile.jpg";
        assertTrue(dataObjectService.existsRootObject(rootObjectName));
        boolean objectExists;

        //when
        objectExists = dataObjectService.existsObject(notExistingFileName);

        //then
        assertFalse(objectExists);
    }

    // Upload Object

    @Test
    void UploadObject_RootObjectExistsNewObject_Uploaded() {
        //given
        assertTrue(dataObjectService.existsRootObject(rootObjectName));
        assertFalse(dataObjectService.existsObject(objectName));

        //when
        dataObjectService.createObject(objectName, testFile);

        //then
        assertTrue(dataObjectService.existsObject(objectName));
    }

    @Test
    void UploadObject_RootObjectExistsObjectAlreadyExists_ThrowException() {
        //given
        assertTrue(dataObjectService.existsRootObject(rootObjectName));
        assertFalse(dataObjectService.existsObject(objectName));

        //when
        dataObjectService.createObject(objectName, testFile);

        //then
        assertTrue(dataObjectService.existsObject(objectName));
        assertThrows(ObjectAlreadyExistsException.class, () -> dataObjectService.createObject(objectName, testFile));
    }

    @Disabled("to not spam bucket manipulation")
    @Test
    void UploadObject_RootObjectDoesntExist_Uploaded() {
        //given
        assertTrue(dataObjectService.existsRootObject(rootObjectName));
        dataObjectService.removeRootObject(rootObjectName, true);
        assertFalse(dataObjectService.existsRootObject(rootObjectName));

        //when
        dataObjectService.createObject(objectName, testFile);

        //then
        assertTrue(dataObjectService.existsRootObject(rootObjectName));
        assertTrue(dataObjectService.existsObject(objectName));
    }

    // Download Object

    @Test
    void DownloadObject_ObjectExists_Downloaded() {
        //given
        assertTrue(dataObjectService.existsRootObject(rootObjectName));
        dataObjectService.createObject(objectName, testFile);
        assertTrue(dataObjectService.existsObject(objectName));
        boolean objectExists;

        //when
        objectExists = dataObjectService.downloadObject(objectName, downloadedImagePath);

        //then
        assertTrue(objectExists);
        var file = new byte[0];
        try {
            file = Files.readAllBytes(downloadedImagePath);
        } catch(Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }
        assertTrue(file.length > 0);
        assertArrayEquals(file, testFile);
    }

    @Test
    void DownloadObject_ObjectDoesntExist_ThrowException() {
        //given
        assertTrue(dataObjectService.existsRootObject(rootObjectName));
        assertFalse(dataObjectService.existsObject(objectName));

        //when
        File file = new File(downloadedImagePath.toUri());

        //then
        assertThrows(ObjectNotFoundException.class, () -> dataObjectService.downloadObject(objectName, downloadedImagePath));
        assertFalse(file.exists());
    }

    // Publish Object

    @Test
    void PublishObject_ObjectExists_Published() {
        //given
        assertTrue(dataObjectService.existsRootObject(rootObjectName));
        dataObjectService.createObject(objectName, testFile);
        assertTrue(dataObjectService.existsObject(objectName));
        String presignedUrl;

        //when
        presignedUrl = dataObjectService.getPresignedUrl(objectName, 60);

        //then
        assertNotNull(presignedUrl);
    }

    @Test
    void PublishObject_ObjectDoesntExist_ThrowException() {
        //given
        assertTrue(dataObjectService.existsRootObject(rootObjectName));
        var notExistingObjectName = "notExistingObject.png";
        assertFalse(dataObjectService.existsObject(notExistingObjectName));

        //then
        assertThrows(ObjectNotFoundException.class, () -> dataObjectService.getPresignedUrl(notExistingObjectName, 60));
    }

    // Remove Object

    @Test
    void RemoveObject_SingleObjectExists_Removed() {
        //given
        assertTrue(dataObjectService.existsRootObject(rootObjectName));
        dataObjectService.createObject(objectName, testFile);
        assertTrue(dataObjectService.existsObject(objectName));

        //when
        dataObjectService.removeObject(objectName, false);

        //then
        assertFalse(dataObjectService.existsObject(objectName));
    }

    @Test
    void RemoveObject_SingleObjectDoesntExist_ThrowException() {
        //given
        var notExistingObjectName = "notExistingObject.png";
        assertTrue(dataObjectService.existsRootObject(rootObjectName));
        assertFalse(dataObjectService.existsObject(notExistingObjectName));

        //then
        assertThrows(ObjectNotFoundException.class, () -> dataObjectService.removeObject(notExistingObjectName, false));
    }

    @Test
    void RemoveObject_FolderObjectExistWithoutRecursiveOption_ThrowException() {
        //given
        var folderObjectName = folderName + objectName;
        assertTrue(dataObjectService.existsRootObject(rootObjectName));
        dataObjectService.createObject(folderObjectName, testFile);
        assertTrue(dataObjectService.existsObject(folderObjectName));

        //then
        assertThrows(NotEmptyException.class, () -> dataObjectService.removeObject(folderName, false));
    }

    @Test
    void RemoveObject_FolderObjectExistWithRecursiveOption_Removed() {
        //given
        var folderObjectName = folderName + objectName;
        assertTrue(dataObjectService.existsRootObject(rootObjectName));
        dataObjectService.createObject(folderObjectName, testFile);
        assertTrue(dataObjectService.existsObject(folderObjectName));
        //when
        dataObjectService.removeObject(folderName, true);

        //then
        assertFalse(dataObjectService.existsObject(folderObjectName));
    }

    @Disabled("to not spam bucket manipulation")
    @Test
    void RemoveObject_RootObjectNotEmptyWithoutRecursiveOption_ThrowException() {
        //given
        assertTrue(dataObjectService.existsRootObject(rootObjectName));
        dataObjectService.createObject(objectName, testFile);
        assertTrue(dataObjectService.existsObject(objectName));

        //then
        assertThrows(NotEmptyException.class, () -> dataObjectService.removeRootObject(rootObjectName, false));
    }

    @Disabled("to not spam bucket manipulation")
    @Test
    void RemoveObject_RootObjectNotEmptyWithRecursiveOption_Removed() {
        //given
        assertTrue(dataObjectService.existsRootObject(rootObjectName));
        dataObjectService.createObject(objectName, testFile);
        assertTrue(dataObjectService.existsObject(objectName));

        //when
        dataObjectService.removeRootObject(rootObjectName, true);

        //then
        assertFalse(dataObjectService.existsRootObject(rootObjectName));
        dataObjectService.createRootObject(rootObjectName);
    }

    @AfterEach
    void tearDown() {
        File file = new File(downloadedImagePath.toUri());
        if (file.exists()) {
            LOG.log(Level.INFO, "{0}", "Deleting file => " + file.delete());
        }

        if (dataObjectService.existsObject(objectName)) {
            dataObjectService.removeObject(objectName, false);
        }

        if (dataObjectService.existsObject(folderName)) {
            dataObjectService.removeObject(folderName, true);
        }
    }

}
