package mx.nopaldev.integrations.services.impl;

import com.google.gson.Gson;
import mx.nopaldev.integrations.services.BlogPostService;
import mx.nopaldev.integrations.services.dtos.PostDto;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultBlogPostService implements BlogPostService {
    private static final Logger LOG = Logger.getLogger(DefaultBlogPostService.class.getName());

    private static final String FAKE_ENDPOINT = "https://jsonplaceholder.typicode.com1/posts";

    @Override
    public List<PostDto> getPosts() throws URISyntaxException, IOException, InterruptedException {
        LOG.log(Level.INFO, "Calling get posts [{0}]", Instant.now());

        // create the request
        final var request = HttpRequest.newBuilder()
                .uri(new URI(FAKE_ENDPOINT))
                .GET()
                .build();

        // call the service and get the raw text from the JSON response
        final var response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString())
                .body();

        final var rawJson = new Gson().fromJson(response, PostDto[].class);

        if (rawJson == null) {
            return null;
        }

        // cast the string into a PostDto array
        // then dump them into a list
        return Arrays.asList(rawJson);
    }
}
