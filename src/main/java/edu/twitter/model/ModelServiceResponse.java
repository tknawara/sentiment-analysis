package edu.twitter.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelServiceResponse {
    private final double label;

    @JsonCreator
    public ModelServiceResponse(@JsonProperty("label") final double label) {
        this.label = label;
    }

    public double getLabel() {
        return label;
    }
}
