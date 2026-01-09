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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status = RoomStatus.ALIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exit_requester_id")
    private User exitRequester; // 종료 요청한 사람


    public static ChatRoom create(User creator){
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.title = generateTitle(); // 수정 필요
        chatRoom.creator = creator;
        chatRoom.participantCode = generateInviteCode();
        chatRoom.observerCode = generateInviteCode();
        chatRoom.status = RoomStatus.ALIVE;
        return chatRoom;
    }

    // 종료 요청
    public void requestExit(User requester) {
        this.exitRequester = requester;
        this.status = RoomStatus.REQUEST_FINISH;
    }

    // 종료 수락
    public void approveExit() {
        this.status = RoomStatus.REQUEST_ACCEPT;
    }

    // 종료 거절
    public void rejectExit() {
        this.exitRequester = null;
        this.status = RoomStatus.ALIVE;
    }

    public void completeReport() {
        this.status = RoomStatus.DONE;
    }

    public boolean isAlive() {
        return this.status == RoomStatus.ALIVE;
    }

    public boolean isRequestFinish() {
        return this.status == RoomStatus.REQUEST_FINISH;
    }

    public boolean isRequestAccept() {
        return this.status == RoomStatus.REQUEST_ACCEPT;
    }

    public boolean isDone() {
        return this.status == RoomStatus.DONE;
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

    public enum RoomStatus {
        ALIVE,        // 정상 운영 중
        REQUEST_FINISH,  // 종료 요청 대기 중
        REQUEST_ACCEPT, // 종료 요청 수락, 판결문 대기
        DONE         // 종료됨
    }
}
