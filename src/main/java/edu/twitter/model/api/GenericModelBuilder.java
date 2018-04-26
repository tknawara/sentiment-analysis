package edu.twitter.model.api;

/**
 * Generic interface for the object responsible for creating any model.
 */
public interface GenericModelBuilder {

    /**
     * Run the recipe responsible for constructing the model.
     *
     * @return an instance of generic model.
     */
    GenericModel build();
}
