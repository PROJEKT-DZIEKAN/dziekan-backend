package com.pbs.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pbs.app.models.FeedPost;
import org.springframework.stereotype.Repository;

import java.util.List;
// import java.util.Optional;
@Repository
public interface FeedPostRepository extends JpaRepository<FeedPost, Long> {
    List<FeedPost> findAllByOrderByPostedAtDesc();
    List<FeedPost> findTop10ByOrderByPostedAtDesc();
    // Metod findById is already provided by JpaRepository, so no need to define it again Nicolas my love
    //    Optional<FeedPost> findById(Long id);
}