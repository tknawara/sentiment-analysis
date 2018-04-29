package edu.twitter.model.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.twitter.model.Label;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class Responsible for communicating
 * with the model service.
 */
public final class ModelClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelClient.class);
    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.createDefault();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int TIME_OUT = 5;
    private static final String API_URL_TEMPLATE = "http://0.0.0.0:%s/%s/classify";

    /**
     * Constructor.
     */
    private ModelClient() {
    }

    /**
     * Perform a Get request to the model's service
     * to get the label of the tweet.
     *
     * @param modelName name of the target model.
     * @param tweet     tweet's text
     * @param port      port of the service model.
     * @return optional of `Label`
     */
    public static Option<Label> callModelService(final String port, final String modelName, final String tweet) {
        final String url = String.format(API_URL_TEMPLATE, port, modelName);
        final ModelRequestBody modelRequestBody = new ModelRequestBody(tweet);
        try {
            return CompletableFuture
                    .supplyAsync(() -> executeRequest(url, modelRequestBody, Label.class))
                    .get(TIME_OUT, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.info("API call timed out");
            return Option.empty();
        }
    }

    /**
     * Execute the API request, then marshall the API response
     * to the given class type.
     *
     * @param url         request url
     * @param requestBody post request body.
     * @param valueType   class to marshall the response to
     * @param <T>         return type
     * @return optional of {@code T}
     */
    private static <T> Option<T> executeRequest(final String url,
                                                final ModelRequestBody requestBody, final Class<T> valueType) {
        try {
            final HttpPost httpPost = new HttpPost(url);
            final StringEntity requestEntity = new StringEntity(MAPPER.writeValueAsString(requestBody));
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(requestEntity);
            final CloseableHttpResponse response = HTTP_CLIENT.execute(httpPost);
            final HttpEntity responseEntity = response.getEntity();
            final T modelServiceResponse =
                    MAPPER.readValue(IOUtils.toString(responseEntity.getContent(), Charset.defaultCharset()), valueType);
            return Option.apply(modelServiceResponse);
        } catch (final IOException e) {
            LOGGER.warn("Error in calling the model service: {}", e);
            return Option.empty();
        }
    }
}
