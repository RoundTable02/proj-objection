package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.dto.CreateChatRoomResponseDto;
import kuit.hackathon.proj_objection.dto.JoinChatRoomResponseDto;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.ChatRoomMember;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.AlreadyJoinedChatRoomException;
import kuit.hackathon.proj_objection.exception.InvalidInviteCodeException;
import kuit.hackathon.proj_objection.repository.ChatRoomMemberRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    // 채팅방 생성
    @Transactional
    public CreateChatRoomResponseDto createChatRoom(User creator) {
        ChatRoom chatRoom = ChatRoom.create(creator);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // 생성자는 자동으로 PARTICIPANT로 입장
        ChatRoomMember creatorMember = ChatRoomMember.create(
                savedChatRoom,
                creator,
                ChatRoomMember.MemberRole.PARTICIPANT
        );
        chatRoomMemberRepository.save(creatorMember);

        return new CreateChatRoomResponseDto(
                savedChatRoom.getId(),
                savedChatRoom.getTitle(),
                savedChatRoom.getParticipantCode(),
                savedChatRoom.getObserverCode()
        );
    }

    // 초대 코드로 채팅방 입장
    @Transactional
    public JoinChatRoomResponseDto joinChatRoom(String inviteCode, User user) {
        // 초대 코드로 채팅방 찾기
        ChatRoom chatRoom = chatRoomRepository.findByInviteCode(inviteCode)
                .orElseThrow(InvalidInviteCodeException::new);

        // 이미 입장했는지 확인
        if (chatRoomMemberRepository.existsByChatRoomAndUser(chatRoom, user)) {
            throw new AlreadyJoinedChatRoomException();
        }

        // 초대 코드 타입에 따라 역할 결정
        ChatRoomMember.MemberRole role = determineRole(chatRoom, inviteCode);

        // 멤버 추가
        ChatRoomMember member = ChatRoomMember.create(chatRoom, user, role);
        chatRoomMemberRepository.save(member);

        return new JoinChatRoomResponseDto(
                chatRoom.getId(),
                chatRoom.getTitle(),
                role.name(),
                role == ChatRoomMember.MemberRole.PARTICIPANT
                        ? "대화 상대방으로 입장했습니다."
                        : "관전자로 입장했습니다."
        );
    }

    // 내가 속한 채팅방 정보 조회
//    @Transactional(readOnly = true)
//    public ChatRoomInfoDto getChatRoomInfo(Long chatRoomId, User user) {
//        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
//                .orElseThrow(ChatRoomNotFoundException::new);
//
//        ChatRoomMember myMember = chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, user)
//                .orElseThrow(ChatRoomMemberNotFoundException::new);
//
//        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoom(chatRoom);
//
//        long participantCount = members.stream()
//                .filter(m -> m.getRole() == ChatRoomMember.MemberRole.PARTICIPANT)
//                .count();
//
//        long observerCount = members.stream()
//                .filter(m -> m.getRole() == ChatRoomMember.MemberRole.OBSERVER)
//                .count();
//
//        return new ChatRoomInfoDto(
//                chatRoom.getId(),
//                chatRoom.getTitle(),
//                myMember.getRole().name(),
//                (int) participantCount,
//                (int) observerCount
//        );
//    }

    // 초대 코드 타입에 따라 역할 결정
    private ChatRoomMember.MemberRole determineRole(ChatRoom chatRoom, String inviteCode) {
        if (inviteCode.equals(chatRoom.getParticipantCode())) {
            return ChatRoomMember.MemberRole.PARTICIPANT;
        } else if (inviteCode.equals(chatRoom.getObserverCode())) {
            return ChatRoomMember.MemberRole.OBSERVER;
        } else {
            throw new InvalidInviteCodeException();
        }
    }
}
