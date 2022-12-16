package ch.heig.amt07.labeldetectorservice.service;

import ch.heig.amt07.labeldetectorservice.dto.LabelWrapper;
import ch.heig.amt07.labeldetectorservice.utils.AwsConfigProvider;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;

@Component
public class AwsLabelDetectorHelper {

    private final RekognitionClient rekClient;
    public final int MAX_LABELS = 10;
    public final double MIN_CONFIDENCE = 90.0;

    public AwsLabelDetectorHelper(AwsConfigProvider configProvider) {
        rekClient = RekognitionClient.builder()
                .region(configProvider.getRegion())
                .credentialsProvider(configProvider.getCredentialsProvider())
                .build();
    }

    public List<LabelWrapper> execute(String imageUri, Optional<Integer> nbLabels, Optional<Double> minConfidence) throws IOException {
        int maxLabels = nbLabels.orElse(MAX_LABELS);
        double minConf = minConfidence.orElse(MIN_CONFIDENCE);

        checkNbLabelsAndMinConfidence(maxLabels, minConf);

        var image = Image.builder()
                .bytes(SdkBytes.fromInputStream(new BufferedInputStream((new URL(imageUri)).openStream())))
                .build();

        List<Label> awsLabels = getLabelsfromImage(image, maxLabels, minConf);
        return LabelWrapper.from(awsLabels);
    }

    public List<LabelWrapper> executeB64(String imageB64, Optional<Integer> nbLabels, Optional<Double> minConfidence) {
        int maxLabels = nbLabels.orElse(MAX_LABELS);
        double minConf = minConfidence.orElse(MIN_CONFIDENCE);

        checkNbLabelsAndMinConfidence(maxLabels, minConf);

        var image = Image.builder()
                .bytes(SdkBytes.fromByteBuffer(ByteBuffer.wrap(java.util.Base64.getDecoder().decode(imageB64))))
                .build();

        List<Label> awsLabels = getLabelsfromImage(image, maxLabels, minConf);
        return LabelWrapper.from(awsLabels);
    }

    private void checkNbLabelsAndMinConfidence(int nbLabels, double minConfidence) throws InvalidParameterException {
        if (nbLabels < 1) {
            throw new InvalidParameterException("nbLabels must be at least 1");
        }
        if (minConfidence < 0 || minConfidence > 100) {
            throw new InvalidParameterException("minConfidence must be between 0 and 100");
        }
    }

    private List<Label> getLabelsfromImage(Image myImage, int nbLabels, double minConfidence)
            throws RekognitionException {
        try {
            DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder()
                    .image(myImage)
                    .maxLabels(nbLabels)
                    .minConfidence((float) minConfidence)
                    .build();

            DetectLabelsResponse labelsResponse = rekClient.detectLabels(detectLabelsRequest);
            return labelsResponse.labels();
        } catch (RekognitionException e) {
            throw RekognitionException.builder().message("Error while detecting labels").build();
        }
    }
}
