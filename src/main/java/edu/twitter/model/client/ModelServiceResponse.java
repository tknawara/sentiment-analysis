package edu.twitter.model.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representation of the model service's response.
 */
public class ModelServiceResponse {
    private final double label;

    /**
     * Constructor.
     *
     * @param label classification label of the tweet.
     */
    @JsonCreator
    public ModelServiceResponse(@JsonProperty("label") final double label) {
        this.label = label;
    }

    public double getLabel() {
        return label;
    }
}
