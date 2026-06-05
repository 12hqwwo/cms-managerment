package com.project.WebAloTra.controller.admin;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.WebAloTra.entity.ChatMessageEntity;
import com.project.WebAloTra.repository.ChatMessageRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ChatHistoryController {

    // ✅ Tiêm repository đúng chuẩn (sửa lỗi cannot be resolved)
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private static final String ADMIN_EMAIL = "admin@gmail.com";

    /**
     * ✅ API: Lấy lịch sử chat giữa admin và 1 user cụ thể
     * Endpoint: /admin/api/chat/history?userEmail={email}
     */
    @GetMapping("/admin/api/chat/history")
    public List<MessageDTO> getHistory(@RequestParam String userEmail) {
        String roomId = buildRoomId(userEmail, ADMIN_EMAIL);

        // Lấy toàn bộ tin nhắn theo roomId, sắp xếp tăng dần
        List<ChatMessageEntity> messages = chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);

        // Chuyển sang DTO gọn cho frontend
        return messages.stream()
                .map(m -> new MessageDTO(m.getSender(), m.getContent()))
                .collect(Collectors.toList());
    }

    /**
     * ✅ API: Lấy danh sách tất cả user đã chat với admin
     * Endpoint: /admin/api/chat/users
     */
    @GetMapping("/admin/api/chat/users")
    public ResponseEntity<List<String>> getAllChatUsers() {
        List<String> users = chatMessageRepository.findDistinctSendersExcludingAdmin();
        return ResponseEntity.ok(users);
    }

    /**
     * ✅ Tạo roomId duy nhất cho cặp user-admin
     */
    private String buildRoomId(String u1, String u2) {
        String a = (u1 == null ? "" : u1.trim().toLowerCase());
        String b = (u2 == null ? "" : u2.trim().toLowerCase());
        return (a.compareTo(b) <= 0) ? a + "|" + b : b + "|" + a;
    }

    /**
     * DTO trả về cho frontend
     */
    @Data
    static class MessageDTO {
        private final String sender;
        private final String content;
    }
}
