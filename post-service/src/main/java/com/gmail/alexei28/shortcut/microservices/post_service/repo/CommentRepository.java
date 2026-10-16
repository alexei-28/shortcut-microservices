package com.gmail.alexei28.shortcut.microservices.post_service.repo;

import com.gmail.alexei28.shortcut.microservices.post_service.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByPostId(Long postId);
}