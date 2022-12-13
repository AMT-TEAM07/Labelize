package ch.heig.amt07.labeldetectorservice.service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record MyLabel (List<LabelWrapper> labels) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyLabel myLabel = (MyLabel) o;
        return labels.equals(myLabel.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labels);
    }
}
