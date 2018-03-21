package edu.twitter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representation of the model service's response.
 */
public enum Label {
    @JsonProperty
    HAPPY(1.0),
    @JsonProperty
    SAD(-1.0);

    private final double label;

    /**
     * Constructor.
     *
     * @param label classification label of the tweet.
     */
    Label(final double label) {
        this.label = label;
    }

    public double getKibanaRepresentation() {
        return label;
    }

}
