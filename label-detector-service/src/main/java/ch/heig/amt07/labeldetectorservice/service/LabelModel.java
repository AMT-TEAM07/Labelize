package ch.heig.amt07.labeldetectorservice.service;

import org.springframework.hateoas.EntityModel;

public class LabelModel extends EntityModel<MyLabel> {
    public LabelModel(MyLabel content) {
        super(content);
    }
}
