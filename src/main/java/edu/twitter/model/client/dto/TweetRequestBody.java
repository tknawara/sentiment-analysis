package edu.twitter.model.client.dto;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Class Representing the post
 * request's body.
 */
public class TweetRequestBody {
    private final String tweetMsg;

    /**
     * Constructor.
     *
     * @param tweetMsg tweet message to send.
     */
    @JsonCreator
    public TweetRequestBody(@JsonProperty("tweetMsg") final String tweetMsg) {
        this.tweetMsg = tweetMsg;
    }

    public String getTweetMsg() {
        return tweetMsg;
    }

    @Override
    public String toString() {
        return "TweetRequestBody{"
                + "tweetMsg='" + tweetMsg
                + '\''
                + '}';
    }
}
