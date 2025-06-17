package com.pbs.app.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pbs.app.models.FeedPost;
import com.pbs.app.repositories.FeedPostRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class FeedServiceImpl implements FeedPostService {

    private final FeedPostRepository feedRepository;

    // Wstrzykiwanie przez konstruktor
    public FeedServiceImpl(FeedPostRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedPost> getAllPostsOrderedByPostedAt() {
        return feedRepository.findAllByOrderByPostedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedPost> getTop10Posts() {
        return feedRepository.findTop10ByOrderByPostedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FeedPost> getPostById(Long id) {
        return feedRepository.findById(id);
    }


    @Override
    @Transactional
    public FeedPost createPost(FeedPost feedPost) {
        return feedRepository.save(feedPost);
    }

    @Override
    @Transactional
    public FeedPost updatePost(Long id, FeedPost feedPost) {
        if (!feedRepository.existsById(id)) {
            throw new EntityNotFoundException("Post o id " + id + " nie istnieje.");
        }
        feedPost.setId(id);
        return feedRepository.save(feedPost);
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        if (!feedRepository.existsById(id)) {
            throw new EntityNotFoundException("Post o id " + id + " nie istnieje.");
        }
        feedRepository.deleteById(id);
    }
}