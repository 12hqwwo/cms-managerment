package com.project.WebAloTra.websocket;

import java.time.Instant;

public class ChatMessage {

    public enum MessageType { CHAT, JOIN, LEAVE }

    private MessageType type = MessageType.CHAT;
    private String from;
    private String to;
    private String content;
    private Instant timestamp = Instant.now();

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
