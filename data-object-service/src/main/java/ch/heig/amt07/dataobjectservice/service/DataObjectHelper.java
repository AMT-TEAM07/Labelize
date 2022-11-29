package ch.heig.amt07.dataobjectservice.service;

import java.nio.file.Path;

public interface DataObjectHelper {
    void createObject(String objectName, Path filePath);

    boolean existsRootObject(String rootObjectName);

    boolean existsObject(String objectName);

    void removeObject(String objectName);

    boolean downloadObject(String objectUrl, Path downloadedImagePath);
}
