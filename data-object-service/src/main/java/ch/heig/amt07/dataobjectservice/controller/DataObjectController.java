package ch.heig.amt07.dataobjectservice.controller;

import ch.heig.amt07.dataobjectservice.exception.NotEmptyException;
import ch.heig.amt07.dataobjectservice.exception.ObjectAlreadyExistsException;
import ch.heig.amt07.dataobjectservice.exception.ObjectNotFoundException;
import ch.heig.amt07.dataobjectservice.service.AwsDataObjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("data-object")
public class DataObjectController {
    private final AwsDataObjectService dataObjectService;

    public DataObjectController(AwsDataObjectService dataObjectService) {
        this.dataObjectService = dataObjectService;
    }

    @PostMapping("upload")
    public ResponseEntity<String> upload(@RequestParam MultipartFile file) {
        try {
            dataObjectService.createObject(file.getOriginalFilename(), file.getBytes());
            return ResponseEntity.ok().body("File uploaded successfully");
        } catch (ObjectAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("publish")
    public ResponseEntity<String> publish(@RequestParam String objectName, @RequestParam(required = false) Optional<Long> expiration) {
        try {
            return ResponseEntity.ok().body(dataObjectService.getPresignedUrl(objectName, expiration));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("delete")
    public ResponseEntity<String> delete(@RequestParam Boolean isRootObject, @RequestParam String objectName, Boolean recursive) {
        try {
            if (isRootObject) {
                if (dataObjectService.existsRootObject(objectName)) {
                    dataObjectService.removeRootObject(objectName, recursive);
                    return ResponseEntity.ok().body("Root object deleted successfully");
                } else {
                    return ResponseEntity.notFound().build();
                }
            } else {
                if (dataObjectService.existsObject(objectName)) {
                    dataObjectService.removeObject(objectName, recursive);
                    return ResponseEntity.ok().body("Object deleted successfully");
                } else {
                    return ResponseEntity.notFound().build();
                }
            }
        } catch (NotEmptyException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
