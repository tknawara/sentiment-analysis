package edu.twitter.model.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

/**
 * Class Responsible for communicating
 * with the model service.
 */
public final class ModelClient {
    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.createDefault();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String API_URL = "http://localhost:8080/classify?tweet=";

    /**
     * Constructor.
     */
    private ModelClient() {
    }

    /**
     * Perform a Get request to the model's service
     * to get the label of the tweet.
     *
     * @param tweet tweet's text
     * @return optional of `ModelServiceResponse`
     */
    public static Optional<ModelServiceResponse> callModelService(final String tweet) {
        final String encodedTweet = new String(Base64.getUrlEncoder().encode(tweet.getBytes(StandardCharsets.UTF_16)));
        return executeRequest(API_URL + encodedTweet, ModelServiceResponse.class);
    }

    /**
     * Execute the API request, then marshall the API response
     * to the given class type.
     *
     * @param url       request url
     * @param valueType class to marshall the response to
     * @param <T>       return type
     * @return optional of {@code T}
     */
    private static <T> Optional<T> executeRequest(final String url, final Class<T> valueType) {
        try {
            final HttpGet httpGet = new HttpGet(url);
            final CloseableHttpResponse response = HTTP_CLIENT.execute(httpGet);
            final HttpEntity entity = response.getEntity();
            final T modelServiceResponse =
                    MAPPER.readValue(IOUtils.toString(entity.getContent(), Charset.defaultCharset()), valueType);
            return Optional.of(modelServiceResponse);
        } catch (final Exception e) {
            System.out.println("API call failed with exception=" + e.getMessage());
            return Optional.empty();
        }
    }
}
