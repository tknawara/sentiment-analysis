package edu.twitter.model.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.twitter.model.client.dto.TweetRequestBody;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.util.Base64;
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

/**
 * Responsible for sending a post requests to any
 * service.
 */
public final class GenericClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericClient.class);
    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.createDefault();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Constructor.
     */
    private GenericClient() {
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
    public static <T> Option<T> executeRequest(final String url,
                                               final TweetRequestBody requestBody, final Class<T> valueType) {
        try {
            final HttpPost httpPost = new HttpPost(url);
            TweetRequestBody encoded = new TweetRequestBody(Base64.encodeBase64String(requestBody.getTweetMsg().getBytes()));
            final StringEntity requestEntity = new StringEntity(MAPPER.writeValueAsString(encoded));
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(requestEntity);
            final CloseableHttpResponse response = HTTP_CLIENT.execute(httpPost);
            final HttpEntity responseEntity = response.getEntity();
            final T modelServiceResponse =
                    MAPPER.readValue(IOUtils.toString(responseEntity.getContent(), Charset.defaultCharset()), valueType);
            return Option.apply(modelServiceResponse);
        } catch (final IOException e) {
            LOGGER.warn("Request body: {}", requestBody);
            LOGGER.warn("Error in calling the model service: {}", e);
            return Option.empty();
        }
    }
}
