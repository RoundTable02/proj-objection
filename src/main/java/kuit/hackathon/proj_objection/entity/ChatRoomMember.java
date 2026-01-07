package kuit.hackathon.proj_objection.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class ChatRoomMember extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    public static ChatRoomMember create(ChatRoom chatRoom, User user, MemberRole role) {
        ChatRoomMember chatRoomMember = new ChatRoomMember();
        chatRoomMember.chatRoom = chatRoom;
        chatRoomMember.user = user;
        chatRoomMember.role = role;
        return chatRoomMember;
    }

    public enum MemberRole {
        PARTICIPANT,
        OBSERVER
    }
}
