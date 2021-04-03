package mx.nopaldev.integrations.services;

import mx.nopaldev.integrations.services.dtos.PostDto;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface BlogPostService {
    List<PostDto> getPosts() throws URISyntaxException, IOException, InterruptedException;
}
