package com.project.WebAloTra.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @Column(name = "sender")
    private String sender; // ví dụ: "user@gmail.com"

    @Column(name = "room_id")
    private String roomId;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "receiver_id")
    private Long receiverId;

    private Boolean seen;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
