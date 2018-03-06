package edu.twitter.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Optional;

public class ModelClient {

    private static final CloseableHttpClient HTTP_CLIENT =
            HttpClients.createDefault();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Optional<ModelServiceResponse> getLabel(String twitterLabel) {
        try {
            return executeRequest("http://localhost:8080/classify?tweet=" + URLEncoder.encode(twitterLabel, "utf-8"), ModelServiceResponse.class);
        } catch (Exception e) {
            return Optional.empty();
        }
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
            final T answersContainer =
                    MAPPER.readValue(IOUtils.toString(entity.getContent(), Charset.defaultCharset()), valueType);
            return Optional.of(answersContainer);
        } catch (final IOException e) {
            System.out.println("API call failed with exception=" + e.getMessage());
            return Optional.empty();
        }
    }
}
