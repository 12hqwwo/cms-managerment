package com.project.WebAloTra.config;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // ⚡ Bắt buộc để kích hoạt STOMP broker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ✅ client JS connect vào /ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // ✅ prefix cho message gửi lên server (frontend -> backend)
        config.setApplicationDestinationPrefixes("/app");
        // ✅ bật broker để server gửi ngược xuống client (backend -> frontend)
        config.enableSimpleBroker("/topic", "/queue");
        config.setUserDestinationPrefix("/user");
    }
    @PostConstruct
    public void init() {
        System.out.println("✅ WebSocketConfig loaded successfully!");
    }
}
