package ch.heig.amt07.labeldetectorservice.controller;

import ch.heig.amt07.labeldetectorservice.service.LabelWrapper;
import ch.heig.amt07.labeldetectorservice.utils.AnalyzeParams;
import org.springframework.web.bind.annotation.*;
import ch.heig.amt07.labeldetectorservice.service.*;

import java.io.IOException;
import java.util.List;

@RestController
class LabelDetectionController{
    private final AwsLabelDetectorHelper labelDetector;

    public LabelDetectionController(AwsLabelDetectorHelper labelDetector) {
        this.labelDetector = labelDetector;
    }

    @PostMapping("/analyze/url")
    List<LabelWrapper> analyzeFromUrl(@RequestBody AnalyzeParams params) throws IOException {
        return labelDetector.execute(params.image(), params.maxLabels(), params.minConfidence());
    }

    @PostMapping("/analyze/b64")
    List<LabelWrapper> analyzeFromB64(@RequestBody AnalyzeParams params) throws IOException {
        return labelDetector.executeB64(params.image(), params.maxLabels(), params.minConfidence());
    }
}