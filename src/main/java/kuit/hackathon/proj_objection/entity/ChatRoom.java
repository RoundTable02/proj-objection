package kuit.hackathon.proj_objection.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Getter
@Entity
public class ChatRoom extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true, nullable = false, length = 9)
    private String participantCode; // 대화 상대방 초대 코드

    @Column(unique = true, nullable = false, length = 9)
    private String observerCode; // 관전자 초대 코드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator; // 방 생성자

    public static ChatRoom create(User creator){
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.title = generateTitle(); // 수정 필요
        chatRoom.creator = creator;
        chatRoom.participantCode = generateInviteCode();
        chatRoom.observerCode = generateInviteCode();
        return chatRoom;
    }

    private static String generateTitle(){
        LocalDateTime now = LocalDateTime.now();

        String year = now.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = now.format(DateTimeFormatter.ofPattern("MM"));
        String day = now.format(DateTimeFormatter.ofPattern("dd"));

        return String.format("%s가단AI%s%s%02d", year, month, day,  new Random().nextInt(100));
    }

    private static String generateInviteCode(){
        Random random = new Random();
        int part1 = random.nextInt(10000);
        int part2 = random.nextInt(10000);

        return String.format("%04d-%04d", part1, part2);
    }


}
