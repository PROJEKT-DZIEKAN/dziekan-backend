package com.pbs.app.controllers;

import com.pbs.app.models.FeedPost;
import com.pbs.app.services.FeedServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/feed-posts")
@RequiredArgsConstructor
public class FeedPostController {
    private final FeedServiceImpl feedPostService;

    @GetMapping("/all")
    public ResponseEntity<List<FeedPost>> getAllPosts() {
        List<FeedPost> posts = feedPostService.getAllPostsOrderedByPostedAt();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/top10")
    public ResponseEntity<List<FeedPost>> getTop10Posts() {
        List<FeedPost> topPosts = feedPostService.getTop10Posts();
        return ResponseEntity.ok(topPosts);
    }

    @GetMapping("{id}")
    public ResponseEntity<FeedPost> getPostById(@PathVariable Long id) {
        return feedPostService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("create")
    public ResponseEntity<FeedPost> createPost(@Valid @RequestBody FeedPost feedPost) {
        FeedPost createdPost = feedPostService.createPost(feedPost);
        return ResponseEntity.ok(createdPost);
    }

    @PutMapping("update/{id}")
    public ResponseEntity<FeedPost> updatePost(@PathVariable Long id, @Valid @RequestBody FeedPost feedPost) {
        try {
            FeedPost updatedPost = feedPostService.updatePost(id, feedPost);
            return ResponseEntity.ok(updatedPost);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        try {
            feedPostService.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
