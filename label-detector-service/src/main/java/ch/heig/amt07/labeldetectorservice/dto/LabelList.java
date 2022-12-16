package ch.heig.amt07.labeldetectorservice.dto;

import java.util.List;
import java.util.Objects;

public record LabelList(List<LabelWrapper> labels) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabelList myLabel = (LabelList) o;
        return labels.equals(myLabel.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labels);
    }
}
