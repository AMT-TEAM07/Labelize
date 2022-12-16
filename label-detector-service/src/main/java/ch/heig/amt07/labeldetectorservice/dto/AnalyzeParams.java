package ch.heig.amt07.labeldetectorservice.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Optional;
public record AnalyzeParams(@NotBlank String image, Optional<Integer> maxLabels, Optional<Double> minConfidence) {
}
