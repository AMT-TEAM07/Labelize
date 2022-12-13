package ch.heig.amt07.labeldetectorservice.controller;

import ch.heig.amt07.labeldetectorservice.service.LabelWrapper;
import ch.heig.amt07.labeldetectorservice.utils.AnalyzeParams;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;
import ch.heig.amt07.labeldetectorservice.service.*;

import java.io.IOException;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
class LabelDetectionController{
    private final AwsLabelDetectorHelper labelDetector;

    public LabelDetectionController(AwsLabelDetectorHelper labelDetector) {
        this.labelDetector = labelDetector;
    }

    @PostMapping("/analyze/url")
    CollectionModel<EntityModel<LabelWrapper>> analyzeFromUrl(@RequestBody AnalyzeParams params) throws IOException {
        List<EntityModel<LabelWrapper>> labels = labelDetector.execute(params.image(), params.maxLabels(), params.minConfidence())
                .stream().map(EntityModel::of).toList();
        return CollectionModel.of(labels, linkTo(methodOn(LabelDetectionController.class).analyzeFromB64(params)).withRel("Base64"));
    }

    @PostMapping("/analyze/b64")
    CollectionModel<EntityModel<LabelWrapper>>  analyzeFromB64(@RequestBody AnalyzeParams params) throws IOException {
        List<EntityModel<LabelWrapper>> labels = labelDetector.executeB64(params.image(), params.maxLabels(), params.minConfidence())
                .stream().map(EntityModel::of).toList();
        return CollectionModel.of(labels, linkTo(methodOn(LabelDetectionController.class).analyzeFromUrl(params)).withRel("fromUrl"));
    }
}