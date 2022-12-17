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

import java.io.FileNotFoundException;
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

public class LabelizeClient {

    private LabelizeClient() {}

    public static HttpRequest createAnalyzeRequest(String jsonStr, String endpoint) throws URISyntaxException {
        return HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8081/analyze/" + endpoint))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonStr))
                .build();
    }
    public static HttpRequest createUploadRequest(Path filePath, String mediatype) throws IOException {
        var fileName = filePath.getFileName().toString();

        var multipartBody = MultipartBodyPublisher.newBuilder()
                .formPart("file", fileName, MoreBodyPublishers.ofMediaType(HttpRequest.BodyPublishers.ofFile(filePath), MediaType.parse(mediatype)))
                .build();
        return MutableRequest.POST("http://localhost:8080/data-object/upload", multipartBody)
                .headers("Content-Type", "multipart/form-data; boundary=" + multipartBody.boundary())
                .build();
    }

    public static HttpRequest createPublishRequest(String objectName, Optional<Long> expiration) throws URISyntaxException {
        var query = "?objectName=" + objectName;
        if (expiration.isPresent()) {
            query += "&expiration=" + expiration.get();
        }

        return HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/data-object/publish" + query))
                .GET()
                .build();
    }

    public static void uploadFile(String fileName, String mediatype) throws IOException, InterruptedException {
        var request = createUploadRequest(Paths.get(fileName), mediatype);
        var response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        switch (response.statusCode()) {
            case 200, 201 -> System.out.println(fileName + " uploaded successfully");
            case 400 -> throw new BadRequestException(response.body());
            case 500 -> throw new InternalServerErrorException(response.body());
            default -> throw new HttpException("Unexpected status code: " + response.statusCode());
        }
    }

    public static String publish(String fileName, Optional<Long> expiration) throws IOException, InterruptedException, URISyntaxException {
        var request = createPublishRequest(fileName, expiration);
        var response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        switch (response.statusCode()) {
            case 200, 201 -> System.out.println(fileName + " published successfully");
            case 400 -> throw new BadRequestException(response.body());
            case 404 -> throw new NotFoundException(response.body());
            case 500 -> throw new InternalServerErrorException(response.body());
            default -> throw new HttpException("Unexpected status code: " + response.statusCode());
        }

        var jsonStr = response.body();
        var mapper = new ObjectMapper();
        var publishJsonNode = mapper.readTree(jsonStr);
        return publishJsonNode.get("signed_url").asText();
    }

    public static String analyze(String signedUrl, String endpoint) throws IOException, InterruptedException, URISyntaxException {
        var request = createAnalyzeRequest(signedUrl, endpoint);
        var response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        switch (response.statusCode()) {
            case 200 -> System.out.println("Analysis successful");
            case 400 -> throw new BadRequestException(response.body());
            case 404 -> throw new NotFoundException(response.body());
            case 500 -> throw new InternalServerErrorException(response.body());
            default -> throw new HttpException("Unexpected status code: " + response.statusCode());
        }

        var jsonStr = response.body();
        System.out.println(jsonStr);
        return jsonStr;
    }

    public static void runScenarioOne() {
        // TODO
    }

    public static void runScenarioTwo() throws IOException, InterruptedException, URISyntaxException {
        // Upload local file to the bucket
        var fileName = "montreux.jpg";
        uploadFile(fileName, "image/jpg");

        // Retrieve a signed url of the uploaded file
        var signedUrl = publish(fileName, Optional.empty());

        // Analyze the image using the signed url
        var jsonStr = String.format("""
                {
                    "image": "%s",
                    "maxLabels": 50,
                    "minConfidence": 55.7
                }""", signedUrl);
        var analysis = analyze(jsonStr, "url");

        // Send result as json to the bucket
        var jsonNamePath = Paths.get("montreux.jpg.json");
        Files.writeString(jsonNamePath, analysis, StandardCharsets.UTF_8);
        uploadFile(jsonNamePath.getFileName().toString(), "application/json");

        // Delete the json file locally
        Files.delete(jsonNamePath);
    }

    public static void runScenarioThree() throws IOException, InterruptedException, URISyntaxException {
        // TODO
    }
}
