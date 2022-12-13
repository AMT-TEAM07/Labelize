package ch.heig.amt07.labeldetectorservice.service;

import org.springframework.hateoas.server.core.Relation;
import software.amazon.awssdk.services.rekognition.model.Label;

import java.util.ArrayList;
import java.util.List;
@Relation(collectionRelation = "labels")
public class LabelWrapper {
    private final String name;
    private final double confidence;

    public LabelWrapper(String name, double confidence) {
        this.name = name;
        this.confidence = confidence;
    }

    public String getName() {
        return name;
    }

    public double getConfidence() {
        return confidence;
    }

    public String toString() {
        return "{Label: " + name + ", Confidence: " + confidence + "}";
    }

    public static List<LabelWrapper> from(List<Label> awsLabels) {
        List<LabelWrapper> labels = new ArrayList<>();
        for (Label label : awsLabels) {
            labels.add(new LabelWrapper(label.name(), label.confidence()));
        }
        return labels;
    }
}
