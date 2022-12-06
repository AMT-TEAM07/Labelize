package ch.heig.amt07.labeldetectorservice.controller;

import ch.heig.amt07.labeldetectorservice.service.LabelWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import ch.heig.amt07.labeldetectorservice.service.*;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
class LabelDetectionController{
    private final AwsLabelDetectorHelper labelDetector;

    public LabelDetectionController(AwsLabelDetectorHelper labelDetector) {
        this.labelDetector = labelDetector;
    }
    @GetMapping("analyze")
    List<LabelWrapper> analyze() throws IOException {
        System.out.println(labelDetector.execute("https://a.cdn-hotels.com/gdcs/production196/d1429/5c2581f0-c31d-11e8-87bb-0242ac11000d.jpg?impolicy=fcrop&w=800&h=533&q=medium"));
        return labelDetector.execute("https://a.cdn-hotels.com/gdcs/production196/d1429/5c2581f0-c31d-11e8-87bb-0242ac11000d.jpg?impolicy=fcrop&w=800&h=533&q=medium");
    }
}