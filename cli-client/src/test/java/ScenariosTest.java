import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static ch.heig.amt07.LabelizeClient.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ScenariosTest {

    @BeforeEach
    void setup() {
        System.out.println("setUp");
    }

    @Disabled("to not spam bucket manipulation")
    @Test
    void Scenario_One_Nothing_Exists() {
        //given

        //when

        //then
    }

    @Test
    void Scenario_Two_Only_RootObject_Exists() {
        /*
        GIVEN
         */
        var fileName = "montreux-test.jpg";
        var jsonName = "montreux-test.jpg.json";

        var filePath = "src/test/resources/" + fileName;
        var jsonNamePath = "src/test/resources/" + jsonName;

        var mediaType = "image/jpg";
        var jsonMediaType = "application/json";

        var jsonStr = """
                {
                    "image": "%s",
                    "maxLabels": 50,
                    "minConfidence": 55.7
                }""";

        AtomicReference<String> signedUrl = new AtomicReference<>("");
        AtomicReference<String> analysis = new AtomicReference<>("");

        /*
        WHEN
         */

        // Upload local file to the bucket
        assertDoesNotThrow(() -> uploadFile(filePath, mediaType));

        // Retrieve a signed url of the uploaded file
        assertDoesNotThrow(() -> signedUrl.set(publish(fileName, Optional.empty())));
        assert (!signedUrl.get().isEmpty());

        // Analyze the image using the signed url
        jsonStr = String.format(jsonStr, signedUrl.get());
        var finalJsonStr = jsonStr;
        assertDoesNotThrow(() -> analysis.set(analyze(finalJsonStr, "url")));
        assert (!analysis.get().isEmpty());

        // Send result as json to the bucket
        assertDoesNotThrow(() -> Files.writeString(Paths.get(jsonNamePath), analysis.get(), StandardCharsets.UTF_8));
        assert (Files.exists(Paths.get(jsonNamePath)));
        assertDoesNotThrow(() -> uploadFile(jsonNamePath, jsonMediaType));

        /*
        THEN
         */

        // Delete the json file locally
        assertDoesNotThrow(() -> Files.delete(Paths.get(jsonNamePath)));
    }

    @Disabled("Not implemented yet")
    @Test
    void Scenario_Three_Everything_Exists() {
        //given

        //when

        //then
    }

    @AfterEach
    void teardown() {
        System.out.println("tearDown");
    }
}
