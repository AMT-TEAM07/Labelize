package ch.heig.amt07;

import ch.heig.amt07.exception.BadRequestException;
import ch.heig.amt07.exception.HttpException;
import ch.heig.amt07.exception.InternalServerErrorException;
import ch.heig.amt07.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mizosoft.methanol.MediaType;
import com.github.mizosoft.methanol.MoreBodyPublishers;
import com.github.mizosoft.methanol.MultipartBodyPublisher;
import com.github.mizosoft.methanol.MutableRequest;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LabelizeClient {

    private static final Logger LOG = Logger.getLogger(LabelizeClient.class.getName());
    private static final String UNEXPECTED_STATUS_CODE = "Unexpected status code: ";

    private LabelizeClient() {
    }

    public static void runScenarioOne() {
        LOG.log(Level.INFO, "Starting scenario 1");

        /*
        GIVEN
         */
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .systemProperties()
                .load();
        assertDoesNotThrow(() -> removeRootObject(dotenv.get("AWS_BUCKET")));

        var fileName = "lausanne.jpg";
        var jsonName = "lausanne.jpg.json";

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
        assertDoesNotThrow(() -> uploadObject(fileName, mediaType));

        // Retrieve a signed url of the uploaded file
        assertDoesNotThrow(() -> signedUrl.set(publish(fileName, Optional.empty())));
        assert (!signedUrl.get().isEmpty());

        // Analyze the image using the signed url
        jsonStr = String.format(jsonStr, signedUrl.get());
        var finalJsonStr = jsonStr;
        assertDoesNotThrow(() -> analysis.set(analyze(finalJsonStr, "url")));
        assert (!analysis.get().isEmpty());

        // Send result as json to the bucket
        assertDoesNotThrow(() -> Files.writeString(Paths.get(jsonName), analysis.get(), StandardCharsets.UTF_8));
        assert (Files.exists(Paths.get(jsonName)));
        assertDoesNotThrow(() -> uploadObject(jsonName, jsonMediaType));

        /*
        THEN
         */

        // Delete the json file locally
        assertDoesNotThrow(() -> Files.delete(Paths.get(jsonName)));

        LOG.log(Level.INFO, "Ending scenario 1");
    }

    public static void runScenarioTwo() {
        LOG.log(Level.INFO, "Starting scenario 2");

        /*
        GIVEN
         */
        var fileName = "montreux.jpg";
        var jsonName = "montreux.jpg.json";
        assertDoesNotThrow(() -> cleanup(fileName, jsonName));

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
        assertDoesNotThrow(() -> uploadObject(fileName, mediaType));

        // Retrieve a signed url of the uploaded file
        assertDoesNotThrow(() -> signedUrl.set(publish(fileName, Optional.empty())));
        assert (!signedUrl.get().isEmpty());

        // Analyze the image using the signed url
        jsonStr = String.format(jsonStr, signedUrl.get());
        var finalJsonStr = jsonStr;
        assertDoesNotThrow(() -> analysis.set(analyze(finalJsonStr, "url")));
        assert (!analysis.get().isEmpty());

        // Send result as json to the bucket
        assertDoesNotThrow(() -> Files.writeString(Paths.get(jsonName), analysis.get(), StandardCharsets.UTF_8));
        assert (Files.exists(Paths.get(jsonName)));
        assertDoesNotThrow(() -> uploadObject(jsonName, jsonMediaType));

        /*
        THEN
         */

        // Delete the json file locally
        assertDoesNotThrow(() -> Files.delete(Paths.get(jsonName)));

        LOG.log(Level.INFO, "Ending scenario 2");
    }

    public static void runScenarioThree() {
        LOG.log(Level.INFO, "Starting scenario 3");

        /*
        GIVEN
         */
        var fileName = "tour-de-peilz.jpg";
        var jsonName = "tour-de-peilz.jpg.json";

        var mediaType = "image/jpg";
        var jsonMediaType = "application/json";

        assertDoesNotThrow(() -> cleanup(fileName, jsonName));
        assertDoesNotThrow(() -> uploadObject(fileName, mediaType));

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

        // Get Bad Request when trying to upload a file that is already in the bucket
        LOG.log(Level.INFO, "Bad Request catched for a file already in the bucket {0}", assertThrows(BadRequestException.class, () -> uploadObject(fileName, mediaType)).getMessage());

        // Retrieve a signed url of the uploaded file
        assertDoesNotThrow(() -> signedUrl.set(publish(fileName, Optional.empty())));
        assert (!signedUrl.get().isEmpty());

        // Analyze the image using the signed url
        jsonStr = String.format(jsonStr, signedUrl.get());
        var finalJsonStr = jsonStr;
        assertDoesNotThrow(() -> analysis.set(analyze(finalJsonStr, "url")));
        assert (!analysis.get().isEmpty());

        // Send result as json to the bucket
        assertDoesNotThrow(() -> Files.writeString(Paths.get(jsonName), analysis.get(), StandardCharsets.UTF_8));
        assert (Files.exists(Paths.get(jsonName)));
        assertDoesNotThrow(() -> uploadObject(jsonName, jsonMediaType));

        /*
        THEN
         */

        // Delete the json file locally
        assertDoesNotThrow(() -> Files.delete(Paths.get(jsonName)));

        LOG.log(Level.INFO, "Ending scenario 3");
    }

    private static HttpRequest createAnalyzeRequest(String jsonStr, String endpoint) throws URISyntaxException {
        return HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8081/analyze/" + endpoint))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonStr))
                .build();
    }

    private static HttpRequest createUploadRequest(Path objectPath, String mediatype) throws IOException {
        var fileName = objectPath.getFileName().toString();

        var multipartBody = MultipartBodyPublisher.newBuilder()
                .formPart("file", fileName, MoreBodyPublishers.ofMediaType(HttpRequest.BodyPublishers.ofFile(objectPath), MediaType.parse(mediatype)))
                .build();
        return MutableRequest.POST("http://localhost:8080/data-object/upload", multipartBody)
                .headers("Content-Type", "multipart/form-data; boundary=" + multipartBody.boundary())
                .build();
    }

    private static HttpRequest createPublishRequest(String objectName, Optional<Long> expiration) throws URISyntaxException {
        var query = "?objectName=" + objectName;
        if (expiration.isPresent()) {
            query += "&expiration=" + expiration.get();
        }

        return HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/data-object/publish" + query))
                .GET()
                .build();
    }

    private static void uploadObject(String objectName, String mediatype) throws IOException, InterruptedException {
        var request = createUploadRequest(Paths.get(objectName), mediatype);
        var response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        switch (response.statusCode()) {
            case 200, 201 -> LOG.log(Level.INFO, "{0}", objectName + " uploaded successfully");
            case 400 -> throw new BadRequestException(response.body());
            case 500 -> throw new InternalServerErrorException(response.body());
            default -> throw new HttpException(UNEXPECTED_STATUS_CODE + response.statusCode());
        }
    }

    private static String publish(String objectName, Optional<Long> expiration) throws IOException, InterruptedException, URISyntaxException {
        var request = createPublishRequest(objectName, expiration);
        var response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        switch (response.statusCode()) {
            case 200, 201 -> LOG.log(Level.INFO, "{0}", objectName + " published successfully");
            case 400 -> throw new BadRequestException(response.body());
            case 404 -> throw new NotFoundException(response.body());
            case 500 -> throw new InternalServerErrorException(response.body());
            default -> throw new HttpException(UNEXPECTED_STATUS_CODE + response.statusCode());
        }

        var jsonStr = response.body();
        var mapper = new ObjectMapper();
        var publishJsonNode = mapper.readTree(jsonStr);
        return publishJsonNode.get("signed_url").asText();
    }

    private static String analyze(String signedUrl, String endpoint) throws IOException, InterruptedException, URISyntaxException {
        var request = createAnalyzeRequest(signedUrl, endpoint);
        var response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        switch (response.statusCode()) {
            case 200 -> LOG.log(Level.INFO, "{0}", "Analysis successful");
            case 400 -> throw new BadRequestException(response.body());
            case 404 -> throw new NotFoundException(response.body());
            case 500 -> throw new InternalServerErrorException(response.body());
            default -> throw new HttpException(UNEXPECTED_STATUS_CODE + response.statusCode());
        }

        var jsonStr = response.body();
        LOG.log(Level.INFO, "Result of analysis : {0}", jsonStr);
        return jsonStr;
    }

    private static void removeRootObject(String rootObjectName) throws IOException, InterruptedException {
        LOG.log(Level.INFO, "Removing root object {0}", rootObjectName);

        var url = "http://localhost:8080/data-object?isRootObject=true&objectName=" + rootObjectName + "&recursive=true";

        // Delete the uploaded file from the bucket
        var request = MutableRequest.create(url)
                .DELETE()
                .build();

        var response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        processStatusCode(rootObjectName, response);
    }

    private static void cleanup(String fileName, String jsonName) throws IOException, InterruptedException {

        LOG.log(Level.INFO, "Start clean up of files from previous runs");

        var fileUrl = "http://localhost:8080/data-object?isRootObject=false&objectName=" + fileName + "&recursive=false";
        var jsonUrl = "http://localhost:8080/data-object?isRootObject=false&objectName=" + jsonName + "&recursive=false";

        // Delete the uploaded file from the bucket
        var request = MutableRequest.create(fileUrl)
                .DELETE()
                .build();

        var response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        processStatusCode(fileName, response);

        // Delete the json file from the bucket
        request = MutableRequest.create(jsonUrl)
                .DELETE()
                .build();
        response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        processStatusCode(jsonName, response);
    }

    private static void processStatusCode(String fileName, HttpResponse<String> response) {
        switch (response.statusCode()) {
            case 200 -> LOG.log(Level.INFO, "{0} deleted successfully", fileName);
            case 400 -> {
                LOG.log(Level.INFO, "Bad request with body : {0}", response.body());
                throw new BadRequestException(response.body());
            }
            case 404 -> LOG.log(Level.INFO, "{0} is not in the bucket", fileName);
            case 500 -> {
                LOG.log(Level.INFO, "Internal server error with body : {0}", response.body());
                throw new InternalServerErrorException(response.body());
            }
            default -> {
                LOG.log(Level.INFO, "Unexpected status code with body : {0}", response.body());
                throw new HttpException(UNEXPECTED_STATUS_CODE + response.statusCode());
            }
        }
    }


}

