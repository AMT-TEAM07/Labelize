package ch.heig.amt07.dataobjectservice.controller;

import ch.heig.amt07.dataobjectservice.exception.NotEmptyException;
import ch.heig.amt07.dataobjectservice.exception.ObjectAlreadyExistsException;
import ch.heig.amt07.dataobjectservice.exception.ObjectNotFoundException;
import ch.heig.amt07.dataobjectservice.model.DataObjectResponse;
import ch.heig.amt07.dataobjectservice.service.AwsDataObjectService;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("data-object")
public class DataObjectController {
    private final AwsDataObjectService dataObjectService;

    public DataObjectController(AwsDataObjectService dataObjectService) {
        this.dataObjectService = dataObjectService;
    }

    @PostMapping("upload")
    public ResponseEntity<EntityModel<DataObjectResponse>> upload(@RequestParam MultipartFile file) {
        var response = new DataObjectResponse();
        response.setObjectName(file.getOriginalFilename());

        var selfLink = linkTo(methodOn(DataObjectController.class).upload(file)).withSelfRel();
        var publishLink = linkTo(methodOn(DataObjectController.class)
                .publish(file.getOriginalFilename(), Optional.empty())).withRel("publish");
        var deleteLink = linkTo(methodOn(DataObjectController.class).delete(false, file.getOriginalFilename(), false)).withRel("delete");

        var entity = EntityModel.of(response, selfLink, publishLink, deleteLink);

        try {
            dataObjectService.createObject(file.getOriginalFilename(), file.getBytes());
            response.setMessage("Object successfully uploaded");
            return ResponseEntity.status(HttpStatus.CREATED).body(entity);
        } catch (ObjectAlreadyExistsException e) {
            response.setMessage("Object already exists");
            return ResponseEntity.badRequest().body(entity);
        } catch (Exception e) {
            response.setMessage("Error while uploading object");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(entity);
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

    @DeleteMapping()
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
