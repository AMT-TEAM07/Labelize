package ch.heig.amt07.labeldetectorservice.controller;

import ch.heig.amt07.labeldetectorservice.service.LabelWrapper;
import ch.heig.amt07.labeldetectorservice.utils.AnalyzeParams;
import ch.heig.amt07.labeldetectorservice.utils.LabelModelAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;
import ch.heig.amt07.labeldetectorservice.service.*;

import java.io.IOException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
public class LabelDetectionController{
    private final AwsLabelDetectorHelper labelDetector;
    private final LabelModelAssembler assembler;

    public LabelDetectionController(AwsLabelDetectorHelper labelDetector) {
        this.labelDetector = labelDetector;
        this.assembler = new LabelModelAssembler();
    }

    @PostMapping("/analyze/url")
    public EntityModel<LabelModel> analyzeFromUrl(@RequestBody AnalyzeParams params) {
        /*List<EntityModel<LabelWrapper>> labels = labelDetector.execute(params.image(), params.maxLabels(), params.minConfidence())
                .stream().map(EntityModel::of).toList();
        return CollectionModel.of(labels, linkTo(methodOn(LabelDetectionController.class).analyzeFromB64(params)).withRel("Base64"));*/
        try{
            MyLabel temp = new MyLabel(labelDetector.execute(params.image(), params.maxLabels(), params.minConfidence()));
            return assembler.toModel(new LabelModel(temp));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/analyze/b64")
    public EntityModel<LabelModel> analyzeFromB64(@RequestBody AnalyzeParams params) {
        MyLabel temp = new MyLabel(labelDetector.executeB64(params.image(), params.maxLabels(), params.minConfidence()));
        return assembler.toModel(new LabelModel(temp));
    }
}