package edu.twitter.model.client.classification;

import edu.twitter.model.client.dto.Label;
import edu.twitter.model.client.GenericClient;
import edu.twitter.model.client.dto.TweetRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class Responsible for communicating
 * with the model service.
 */
public final class ClassificationClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationClient.class);
    private static final int TIME_OUT = 5;
    private static final String API_URL_TEMPLATE = "http://0.0.0.0:%s/%s/classify";

    /**
     * Constructor.
     */
    private ClassificationClient() {
    }

    /**
     * Perform a Post request to the model's service
     * to get the label of the tweet.
     *
     * @param modelName name of the target model.
     * @param tweet     tweet's text
     * @param port      port of the service model.
     * @return optional of `Label`
     */
    public static Option<Label> callModelService(final String port, final String modelName, final String tweet) {
        final String url = String.format(API_URL_TEMPLATE, port, modelName);
        final TweetRequestBody tweetRequestBody = new TweetRequestBody(tweet);
        try {
            return CompletableFuture
                    .supplyAsync(() -> GenericClient.executeRequest(url, tweetRequestBody, Label.class))
                    .get(TIME_OUT, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.info("API call timed out");
            return Option.empty();
        }
    }

}
