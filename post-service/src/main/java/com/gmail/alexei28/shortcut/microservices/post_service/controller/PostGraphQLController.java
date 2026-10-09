package com.gmail.alexei28.shortcut.microservices.post_service.controller;

import com.gmail.alexei28.shortcut.microservices.post_service.entity.PostEntity;
import com.gmail.alexei28.shortcut.microservices.post_service.service.PostService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class PostGraphQLController {

    private final PostService postService;

    public PostGraphQLController(PostService postService) {
        this.postService = postService;
    }

    @QueryMapping
    public PostEntity postById(@Argument Long id) {
        return postService.getPostById(id);
    }
}