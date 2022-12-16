package ch.heig.amt07.labeldetectorservice.model;

import ch.heig.amt07.labeldetectorservice.dto.LabelList;
import org.springframework.hateoas.EntityModel;

public class LabelModel extends EntityModel<LabelList> {
    public LabelModel(LabelList content) {
        super(content);
    }
}
