package edu.twitter.model.api;

import edu.twitter.model.Label;

/**
 * Generic interface for sentiment analysis model.
 */
public interface GenericModel {

    /**
     * @return model name
     */
    String name();

    /**
     * Classify the given tweet.
     *
     * @param tweetText target tweet message for classification.
     * @return 0 for sad & 1 for happy
     */
    Label getLabel(String tweetText);
}
