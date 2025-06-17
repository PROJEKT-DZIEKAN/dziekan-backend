package com.pbs.app.services;
import java.util.List;
import java.util.Optional;

import com.pbs.app.models.FeedPost;
public interface FeedPostService {
    List<FeedPost> getAllPostsOrderedByPostedAt();
    List<FeedPost> getTop10Posts();
    Optional<FeedPost> getPostById(Long id);

    FeedPost createPost(FeedPost feedPost);
    FeedPost updatePost(Long id, FeedPost feedPost);

    void deletePost(Long id);
}