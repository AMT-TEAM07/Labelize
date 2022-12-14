package ch.heig.amt07.labeldetectorservice.controller;

import ch.heig.amt07.labeldetectorservice.dto.LabelList;
import ch.heig.amt07.labeldetectorservice.model.LabelModel;
import ch.heig.amt07.labeldetectorservice.dto.AnalyzeParams;
import ch.heig.amt07.labeldetectorservice.assembler.LabelModelAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ch.heig.amt07.labeldetectorservice.service.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;


@RestController
@RequestMapping("/v1/label-detector-management/analyze")
public class LabelDetectionController{
    private final AwsLabelDetector labelDetector;
    private final LabelModelAssembler assembler;

    public LabelDetectionController(AwsLabelDetector labelDetector) {
        this.labelDetector = labelDetector;
        this.assembler = new LabelModelAssembler();
    }

    @PostMapping("/url")
    public EntityModel<LabelModel> analyzeFromUrl(@RequestBody AnalyzeParams params) {
        try{
            LabelList temp = new LabelList(labelDetector.execute(params.image(), params.maxLabels(), params.minConfidence()));
            return assembler.toModel(new LabelModel(temp));
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage()
            );
        }
    }

    @PostMapping("/b64")
    public EntityModel<LabelModel> analyzeFromB64(@RequestBody AnalyzeParams params) {
        LabelList temp = new LabelList(labelDetector.executeB64(params.image(), params.maxLabels(), params.minConfidence()));
        return assembler.toModel(new LabelModel(temp));
    }
}