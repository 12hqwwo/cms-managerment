package com.project.WebAloTra.websocket;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatViewController {

    @GetMapping("/chat")
    public String showChatPage() {
        // ⚠️ file HTML đang ở templates/user/chat.html
        return "user/chat";
    }
}
