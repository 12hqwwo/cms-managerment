package com.project.WebAloTra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.WebAloTra.entity.ChatMessageEntity;
import com.project.WebAloTra.repository.ChatMessageRepository;

import java.util.List;

@Controller
public class ChatAdminController {

    @Autowired
    private ChatMessageRepository chatRepo;

    @GetMapping("/admin/chat")
    public String chatPage(Model model) {
        // ✅ Lấy toàn bộ tin nhắn, sắp xếp theo thời gian tăng dần
        List<ChatMessageEntity> messages = chatRepo.findAllByOrderByCreateDateAsc();

        model.addAttribute("messages", messages);

        return "admin/chat-admin";
    }
}
