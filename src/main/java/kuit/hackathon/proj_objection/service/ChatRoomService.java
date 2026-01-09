package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.dto.*;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.ChatRoomMember;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.*;
import kuit.hackathon.proj_objection.repository.ChatRoomMemberRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;

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

        String roleKorean = role == ChatRoomMember.MemberRole.PARTICIPANT ? "대화 상대방" : "관전자";
        JoinNotificationDto notification = new JoinNotificationDto(
                "USER_JOINED",
                user.getNickname(),
                role.name(),
                user.getNickname() + "님이 " + roleKorean + "으로 입장했습니다."
        );

        messagingTemplate.convertAndSend("/topic/chatroom/" + chatRoom.getId() + "/join", notification);

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

    // 종료 요청
    @Transactional
    public ExitRequestResponseDto requestExit(Long chatRoomId, User requester) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        // 종료된 채팅방 확인 -> 이미 종료된 채팅방을 또 종료하려고 하는가
        if (chatRoom.getStatus() == ChatRoom.RoomStatus.CLOSED) {
            throw new ChatRoomClosedException();
        }

        // 요청자가 PARTICIPANT인지 확인
        ChatRoomMember requesterMember = chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, requester)
                .orElseThrow(ChatRoomMemberNotFoundException::new);

        if (requesterMember.getRole() != ChatRoomMember.MemberRole.PARTICIPANT) {
            throw new ExitRequestPermissionDeniedException();
        }

        // 종료 요청 상태로 변경
        chatRoom.requestExit(requester);
        chatRoomRepository.save(chatRoom);

        // WebSocket으로 다른 PARTICIPANT들에게 알림 -> 브로드캐스트용
        ExitNotificationDto notification = new ExitNotificationDto(
                "EXIT_REQUEST",
                requester.getNickname(),
                "지금까지의 대화를 바탕으로 판결을 요청하시겠습니까?"
        );
        messagingTemplate.convertAndSend("/topic/chatroom/" + chatRoomId + "/exit", notification);

        // 요청 응답
        return new ExitRequestResponseDto(
                chatRoomId,
                requester.getNickname(),
                "판결 요청이 전송되었습니다."
        );
    }

    // 종료 수락/거절
    @Transactional
    public ExitDecisionResponseDto decideExit(Long chatRoomId, User decider, Boolean approve) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        // 종료 요청 확인
        if (chatRoom.getStatus() != ChatRoom.RoomStatus.EXIT_PENDING) {
            throw new NoExitRequestException();
        }

        // 결정자가 PARTICIPANT인지 확인
        ChatRoomMember deciderMember = chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, decider)
                .orElseThrow(ChatRoomMemberNotFoundException::new);

        if (deciderMember.getRole() != ChatRoomMember.MemberRole.PARTICIPANT) {
            throw new ExitDecisionPermissionDeniedException();
        }

        // 본인이 요청한 종료는 처리 불가
        if (chatRoom.getExitRequester().getId().equals(decider.getId())) {
            throw new ExitDecisionPermissionDeniedException();
        }

        String message;
        String notificationType;

        if (approve) {
            // 수락
            chatRoom.approveExit();
            message = "판결이 확정되었습니다.";
            notificationType = "EXIT_APPROVED";
        } else {
            // 거절
            chatRoom.rejectExit();
            message = "판결 요청이 거절되었습니다.";
            notificationType = "EXIT_REJECTED";
        }

        chatRoomRepository.save(chatRoom);

        // WebSocket으로 알림
        ExitNotificationDto notification = new ExitNotificationDto(
                notificationType,
                decider.getNickname(),
                message
        );
        messagingTemplate.convertAndSend("/topic/chatroom/" + chatRoomId + "/exit", notification);

        return new ExitDecisionResponseDto(
                chatRoomId,
                approve,
                message
        );
    }

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
