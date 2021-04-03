package mx.nopaldev.integrations.facades;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import mx.nopaldev.integrations.services.BlogPostService;
import mx.nopaldev.integrations.services.dtos.PostDto;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlogPostFacade {
    private static final Logger LOG = Logger.getLogger(BlogPostFacade.class.getName());

    private final BlogPostService blogPostService;
    private final Retry retryConfig;

    public BlogPostFacade(final BlogPostService blogPostService) {
        this.blogPostService = blogPostService;
        this.retryConfig = initRetryConfig();
    }

    private Retry initRetryConfig() {
        LOG.log(Level.INFO, "Creating the retry config");

        // define when we should retry calling the service
        final var config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .retryOnException(e -> {
                    // just log the exception type as a way to see
                    // what exactly is causing the retries
                    LOG.log(Level.WARNING,
                            "Exception [{0}] caught during third party execution.",
                            e.getClass().getSimpleName());

                    return e instanceof IOException || e instanceof InterruptedException;
                })
                .failAfterMaxAttempts(true)
                .build();

        // register our custom configuration
        final var registry = RetryRegistry.of(config);

        // create the registry
        return registry.retry("few-retries-config");
    }

    public void pullPosts() {
        List<PostDto> posts = null;
        // call the service
        try {
            posts = fetchPostsRemotely();
        } catch (final Throwable ex) {
            LOG.log(Level.SEVERE, "Error while trying to call service", ex);
        }

        // process the response
        if (posts != null) {
            posts.forEach(post -> LOG.log(Level.INFO, post.toString()));
        }
    }

    private List<PostDto> fetchPostsRemotely() throws Throwable {
        // indicate that this service will have the retry config applied to it
        final var handledSupplier = Retry.decorateCheckedSupplier(retryConfig,
                getBlogPostService()::getPosts);

        return handledSupplier.apply();
    }


    public BlogPostService getBlogPostService() {
        return blogPostService;
    }
}
