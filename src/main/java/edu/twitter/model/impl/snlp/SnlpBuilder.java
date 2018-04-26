package edu.twitter.model.impl.snlp;

import edu.twitter.model.api.GenericModel;
import edu.twitter.model.api.GenericModelBuilder;

/**
 * Builder for Stanford nlp model.
 */
public class SnlpBuilder implements GenericModelBuilder {

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericModel build() {
        return new SnlpModel();
    }
}
