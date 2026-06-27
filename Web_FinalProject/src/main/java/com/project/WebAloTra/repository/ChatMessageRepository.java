package com.project.WebAloTra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.project.WebAloTra.entity.ChatMessageEntity;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    // ✅ Dành cho trang admin load tất cả tin nhắn
    List<ChatMessageEntity> findAllByOrderByCreateDateAsc();

    // ✅ Dành cho /admin/api/chat/history?userEmail=... để lấy lịch sử riêng từng user
    List<ChatMessageEntity> findByRoomIdOrderByCreatedAtAsc(String roomId);
    
    @Query("SELECT DISTINCT m.sender FROM ChatMessageEntity m WHERE m.sender <> 'admin@gmail.com'")
    List<String> findDistinctSendersExcludingAdmin();
}
