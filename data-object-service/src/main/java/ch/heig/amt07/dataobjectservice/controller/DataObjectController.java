package ch.heig.amt07.dataobjectservice.controller;

import ch.heig.amt07.dataobjectservice.exception.NotEmptyException;
import ch.heig.amt07.dataobjectservice.exception.ObjectAlreadyExistsException;
import ch.heig.amt07.dataobjectservice.exception.ObjectNotFoundException;
import ch.heig.amt07.dataobjectservice.dto.DataObjectResponse;
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
            return ResponseEntity.internalServerError().body(entity);
        }
    }

    @GetMapping("publish")
    public ResponseEntity<EntityModel<DataObjectResponse>> publish(@RequestParam String objectName, @RequestParam(required = false) Optional<Long> expiration) {
        var response = new DataObjectResponse();
        response.setObjectName(objectName);
        response.setExpiration(expiration);

        var selfLink = linkTo(methodOn(DataObjectController.class)
                .publish(objectName, Optional.empty())).withSelfRel();

        var uploadLink = linkTo(methodOn(DataObjectController.class).upload(null)).withRel("upload");
        var deleteLink = linkTo(methodOn(DataObjectController.class).delete(false, objectName, false)).withRel("delete");

        var entity = EntityModel.of(response, selfLink, uploadLink, deleteLink);
        try {
            var url = dataObjectService.getPresignedUrl(objectName, expiration);
            var expr = expiration.orElse(AwsDataObjectService.MIN_EXPIRATION_IN_SECONDS);
            response.setMessage("Object successfully published");
            response.setSignedUrl(Optional.of(url));
            response.setExpiration(Optional.of(expr));
            return ResponseEntity.ok().body(entity);
        } catch (IllegalArgumentException e) {
            response.setMessage("Invalid expiration time");
            return ResponseEntity.badRequest().body(entity);
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            response.setMessage("Error while publishing object");
            return ResponseEntity.internalServerError().body(entity);
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
