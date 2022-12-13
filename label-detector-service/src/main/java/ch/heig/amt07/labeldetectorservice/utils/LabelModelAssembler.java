package ch.heig.amt07.labeldetectorservice.utils;

import ch.heig.amt07.labeldetectorservice.controller.LabelDetectionController;
import ch.heig.amt07.labeldetectorservice.service.LabelModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class LabelModelAssembler implements RepresentationModelAssembler<LabelModel, EntityModel<LabelModel>> {

    @Override
    public EntityModel<LabelModel> toModel(LabelModel entities) {
        return EntityModel.of(entities, linkTo(methodOn(LabelDetectionController.class).analyzeFromUrl(
                new AnalyzeParams("randomImage", Optional.empty(), Optional.empty())
        )).withRel("url"), linkTo(methodOn(LabelDetectionController.class).analyzeFromB64(
                new AnalyzeParams("randomImage", Optional.empty(), Optional.empty())
        )).withRel("base64"));
    }
}
