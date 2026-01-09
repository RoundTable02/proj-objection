package kuit.hackathon.proj_objection.repository;

import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.ChatRoomMember;
import kuit.hackathon.proj_objection.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    // 특정 채팅방의 특정 사용자 멤버 찾기
    Optional<ChatRoomMember> findByChatRoomAndUser(ChatRoom chatRoom, User user);

    // 특정 채팅방의 모든 멤버 조회
    List<ChatRoomMember> findByChatRoom(ChatRoom chatRoom);

    // 특정 채팅방에 특정 사용자가 이미 참여했는지 확인
    boolean existsByChatRoomAndUser(ChatRoom chatRoom, User user);
}
