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
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AwsDataObjectServiceTests {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    private AwsDataObjectService rootObjectManager;
    private String rootObjectName;
    private Path testImagePath;
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
        testImagePath = Paths.get("src", "test", "resources", objectName);
        downloadedImagePath = Paths.get("src", "test", "resources", "downloaded-" + objectName);

        var configProvider = new AwsConfigProvider("TEST_AWS_ACCESS_KEY_ID", "TEST_AWS_SECRET_ACCESS_KEY", "TEST_AWS_DEFAULT_REGION");
        rootObjectManager = new AwsDataObjectService(configProvider, rootObjectName);
    }

    // DoesExist

    @Test
    void DoesObjectExist_RootObjectExists_Exists() {
        //given
        var existingRootObjectName = rootObjectName;
        boolean rootObjectExists;

        //when
        rootObjectExists = rootObjectManager.existsRootObject(existingRootObjectName);

        //then
        assertTrue(rootObjectExists);
    }

    @Test
    void DoesObjectExist_RootObjectDoesntExist_DoesntExist() {
        //given
        var notExistingRootObjectName = "notExistingRootObjectName" + rootObjectName;
        boolean rootObjectExists;

        //when
        rootObjectExists = rootObjectManager.existsRootObject(notExistingRootObjectName);

        //then
        assertFalse(rootObjectExists);
    }

    @Test
    void DoesObjectExist_RootObjectAndObjectExist_Exists() {
        //given
        assertTrue(rootObjectManager.existsRootObject(rootObjectName));
        rootObjectManager.createObject(objectName, testImagePath);
        boolean objectExists;

        //when
        objectExists = rootObjectManager.existsObject(objectName);

        //then
        assertTrue(objectExists);
    }

    @Test
    void DoesObjectExist_RootObjectExistObjectDoesntExist_DoesntExist() {
        //given
        String notExistingFileName = "notExistingFile.jpg";
        assertTrue(rootObjectManager.existsRootObject(rootObjectName));
        boolean objectExists;

        //when
        objectExists = rootObjectManager.existsObject(notExistingFileName);

        //then
        assertFalse(objectExists);
    }

    // Upload Object

    @Test
    void UploadObject_RootObjectExistsNewObject_Uploaded() {
        //given
        assertTrue(rootObjectManager.existsRootObject(rootObjectName));
        assertFalse(rootObjectManager.existsObject(objectName));

        //when
        rootObjectManager.createObject(objectName, testImagePath);

        //then
        assertTrue(rootObjectManager.existsObject(objectName));
    }

    @Test
    void UploadObject_RootObjectExistsObjectAlreadyExists_ThrowException() {
        //given
        assertTrue(rootObjectManager.existsRootObject(rootObjectName));
        assertFalse(rootObjectManager.existsObject(objectName));

        //when
        rootObjectManager.createObject(objectName, testImagePath);

        //then
        assertTrue(rootObjectManager.existsObject(objectName));
        assertThrows(ObjectAlreadyExistsException.class, () -> rootObjectManager.createObject(objectName, testImagePath));
    }

    @Disabled("to not spam bucket manipulation")
    @Test
    void UploadObject_RootObjectDoesntExist_Uploaded() {
        //given
        assertTrue(rootObjectManager.existsRootObject(rootObjectName));
        rootObjectManager.removeRootObject(rootObjectName, true);
        assertFalse(rootObjectManager.existsRootObject(rootObjectName));

        //when
        rootObjectManager.createObject(objectName, testImagePath);

        //then
        assertTrue(rootObjectManager.existsRootObject(rootObjectName));
        assertTrue(rootObjectManager.existsObject(objectName));
    }

    // Download Object

    @Test
    void DownloadObject_ObjectExists_Downloaded() {
        //given
        assertTrue(rootObjectManager.existsRootObject(rootObjectName));
        rootObjectManager.createObject(objectName, testImagePath);
        assertTrue(rootObjectManager.existsObject(objectName));
        boolean objectExists;

        //when
        objectExists = rootObjectManager.downloadObject(objectName, downloadedImagePath);

        //then
        assertTrue(objectExists);
        File file = new File(downloadedImagePath.toUri());
        assertTrue(file.exists());
        assertEquals(file.length(), testImagePath.toFile().length());
    }

    @Test
    void DownloadObject_ObjectDoesntExist_ThrowException() {
        //given
        assertTrue(rootObjectManager.existsRootObject(rootObjectName));
        assertFalse(rootObjectManager.existsObject(objectName));

        //when
        File file = new File(downloadedImagePath.toUri());

        //then
        assertThrows(ObjectNotFoundException.class, () -> rootObjectManager.downloadObject(objectName, downloadedImagePath));
        assertFalse(file.exists());
    }

    // Publish Object

    @Test
    void PublishObject_ObjectExists_Published() {
        //given
        assertTrue(rootObjectManager.existsRootObject(rootObjectName));
        rootObjectManager.createObject(objectName, testImagePath);
        assertTrue(rootObjectManager.existsObject(objectName));
        String presignedUrl;

        //when
        presignedUrl = rootObjectManager.getPresignedUrl(objectName, 60);

        //then
        assertNotNull(presignedUrl);
    }

    @Test
    void PublishObject_ObjectDoesntExist_ThrowException() {
        //given
        assertTrue(rootObjectManager.existsRootObject(rootObjectName));
        var notExistingObjectName = "notExistingObject.png";
        assertFalse(rootObjectManager.existsObject(notExistingObjectName));

        //then
        assertThrows(ObjectNotFoundException.class, () -> rootObjectManager.getPresignedUrl(notExistingObjectName, 60));
    }

    // Remove Object

    @Test
    void RemoveObject_SingleObjectExists_Removed() {
        //given
        assertTrue(rootObjectManager.existsRootObject(rootObjectName));
        rootObjectManager.createObject(objectName, testImagePath);
        assertTrue(rootObjectManager.existsObject(objectName));

        //when
        rootObjectManager.removeObject(objectName, false);

        //then
        assertFalse(rootObjectManager.existsObject(objectName));
    }

    @Test
    void RemoveObject_SingleObjectDoesntExist_ThrowException() {
        //given
        var notExistingObjectName = "notExistingObject.png";
        assertTrue(rootObjectManager.existsRootObject(rootObjectName));
        assertFalse(rootObjectManager.existsObject(notExistingObjectName));

        //then
        assertThrows(ObjectNotFoundException.class, () -> rootObjectManager.removeObject(notExistingObjectName, false));
    }

    @Test
    void RemoveObject_FolderObjectExistWithoutRecursiveOption_ThrowException() {
        //given
        var folderObjectName = folderName + objectName;
        assertTrue(rootObjectManager.existsRootObject(rootObjectName));
        rootObjectManager.createObject(folderObjectName, testImagePath);
        assertTrue(rootObjectManager.existsObject(folderObjectName));

        //then
        assertThrows(NotEmptyException.class, () -> rootObjectManager.removeObject(folderName, false));
    }

    @Test
    void RemoveObject_FolderObjectExistWithRecursiveOption_Removed() {
        //given
        var folderObjectName = folderName + objectName;
        assertTrue(rootObjectManager.existsRootObject(rootObjectName));
        rootObjectManager.createObject(folderObjectName, testImagePath);
        assertTrue(rootObjectManager.existsObject(folderObjectName));
        //when
        rootObjectManager.removeObject(folderName, true);

        //then
        assertFalse(rootObjectManager.existsObject(folderObjectName));
    }

    @Disabled("to not spam bucket manipulation")
    @Test
    void RemoveObject_RootObjectNotEmptyWithoutRecursiveOption_ThrowException() {
        //given
        assertTrue(rootObjectManager.existsRootObject(rootObjectName));
        rootObjectManager.createObject(objectName, testImagePath);
        assertTrue(rootObjectManager.existsObject(objectName));

        //then
        assertThrows(NotEmptyException.class, () -> rootObjectManager.removeRootObject(rootObjectName, false));
    }

    @Disabled("to not spam bucket manipulation")
    @Test
    void RemoveObject_RootObjectNotEmptyWithRecursiveOption_Removed() {
        //given
        assertTrue(rootObjectManager.existsRootObject(rootObjectName));
        rootObjectManager.createObject(objectName, testImagePath);
        assertTrue(rootObjectManager.existsObject(objectName));

        //when
        rootObjectManager.removeRootObject(rootObjectName, true);

        //then
        assertFalse(rootObjectManager.existsRootObject(rootObjectName));
        rootObjectManager.createRootObject(rootObjectName);
    }

    @AfterEach
    void tearDown() {
        File file = new File(downloadedImagePath.toUri());
        if (file.exists()) {
            LOG.log(Level.INFO, "{0}", "Deleting file => " + file.delete());
        }

        if (rootObjectManager.existsObject(objectName)) {
            rootObjectManager.removeObject(objectName, false);
        }

        if (rootObjectManager.existsObject(folderName)) {
            rootObjectManager.removeObject(folderName, true);
        }
    }

}
