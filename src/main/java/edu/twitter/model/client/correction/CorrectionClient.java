package edu.twitter.model.client.correction;

import edu.twitter.model.client.GenericClient;
import edu.twitter.model.client.dto.TweetRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.compat.java8.JFunction1;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for communicating with the tweet
 * correction service.
 */
public final class CorrectionClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(CorrectionClient.class);
    private static final int TIME_OUT = 5;
    private static final String API_URL_TEMPLATE = "http://0.0.0.0:%s/%s/correct";

    /**
     * Constructor.
     */
    private CorrectionClient() {
    }

    /**
     * Perform a Post request to the correction service to get the
     * corrected tweet.
     *
     * @param port            service port number
     * @param serviceProvider name of the target provider
     * @param tweet           target tweet for correction
     * @return the corrected tweet
     */
    public static Option<String> callCorrectionService(final String port,
                                                       final String serviceProvider, final String tweet) {
        final String url = String.format(API_URL_TEMPLATE, port, serviceProvider);
        final TweetRequestBody tweetRequestBody = new TweetRequestBody(tweet);
        final JFunction1<TweetRequestBody, String> extractor = TweetRequestBody::getTweetMsg;
        try {
            return CompletableFuture
                    .supplyAsync(() -> GenericClient.executeRequest(url, tweetRequestBody, TweetRequestBody.class))
                    .get(TIME_OUT, TimeUnit.SECONDS)
                    .map(extractor);
        } catch (final Exception e) {
            LOGGER.info("API call timed out");
            return Option.empty();
        }
    }
}
