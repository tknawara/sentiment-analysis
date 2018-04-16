package edu.twitter.model.client;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Class Representing the post
 * request's body.
 */
public class ModelRequestBody {
    private final String tweetMsg;

    /**
     * Constructor.
     *
     * @param tweetMsg tweet message to send.
     */
    @JsonCreator
    public ModelRequestBody(@JsonProperty("tweetMsg") final String tweetMsg) {
        this.tweetMsg = tweetMsg;
    }

    public String getTweetMsg() {
        return tweetMsg;
    }
}
