package ch.heig.amt07.dataobjectservice.controller;

import ch.heig.amt07.dataobjectservice.service.AwsDataObjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("data-object")
public class DataObjectController {
    private final AwsDataObjectService dataObjectService;

    public DataObjectController(AwsDataObjectService dataObjectService) {
        this.dataObjectService = dataObjectService;
    }

    @PostMapping("upload")
    public ResponseEntity<Object> upload(@RequestParam MultipartFile file) {
        try {
            dataObjectService.createObject(file.getOriginalFilename(), file.getBytes());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body("File uploaded successfully");
    }
}
