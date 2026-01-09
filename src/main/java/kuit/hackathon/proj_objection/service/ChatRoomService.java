package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.dto.response.CreateChatRoomResponseDto;
import kuit.hackathon.proj_objection.dto.response.ExitDecisionResponseDto;
import kuit.hackathon.proj_objection.dto.response.ExitRequestResponseDto;
import kuit.hackathon.proj_objection.dto.response.JoinChatRoomResponseDto;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.ChatRoomMember;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.*;
import kuit.hackathon.proj_objection.repository.ChatRoomMemberRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final AsyncJudgmentService asyncJudgmentService;
    private final ChatRoomCacheService chatRoomCacheService;

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

        if(chatRoomMemberRepository.countByChatRoomAndRoleAndUserNot(chatRoom, ChatRoomMember.MemberRole.PARTICIPANT, user)==2){
            throw new ChatRoomFullException();
        }

        // 이미 입장했는지 확인
        ChatRoomMember member = chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, user)
                .orElseGet(() -> {
                    // 아직 멤버가 아니면 새로 생성
                    ChatRoomMember.MemberRole role = determineRole(chatRoom, inviteCode);
                    ChatRoomMember newMember = ChatRoomMember.create(chatRoom, user, role);
                    return chatRoomMemberRepository.save(newMember);
                });


        ChatRoomMember.MemberRole role = member.getRole();

        // PARTICIPANT 입장 시 Redis percent 캐시 갱신
        if (role == ChatRoomMember.MemberRole.PARTICIPANT) {
            try {
                Map<String, Integer> percent = chatRoomMemberRepository.findByChatRoom(chatRoom).stream()
                        .filter(m -> m.getRole() == ChatRoomMember.MemberRole.PARTICIPANT)
                        .collect(java.util.stream.Collectors.toMap(
                                m -> m.getUser().getNickname(),
                                ChatRoomMember::getPercent
                        ));
                chatRoomCacheService.setPercent(chatRoom.getId(), percent);
            } catch (Exception e) {
                log.warn("Failed to update percent cache for room {}: {}", chatRoom.getId(), e.getMessage());
            }
        }

        return new JoinChatRoomResponseDto(
                chatRoom.getId(),
                chatRoom.getTitle(),
                role.name(),
                role == ChatRoomMember.MemberRole.PARTICIPANT
                        ? "대화 상대방으로 입장했습니다."
                        : "관전자로 입장했습니다."
        );
    }


    // 종료 요청
    @Transactional
    public ExitRequestResponseDto requestExit(Long chatRoomId, User requester) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        // 종료된 채팅방 확인 -> 이미 종료된 채팅방을 또 종료하려고 하는가
        if (chatRoom.isDone()){
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

        // Redis status 캐시 업데이트
        try {
            String requesterNickname = chatRoom.getExitRequester().getNickname();
            chatRoomCacheService.setStatus(chatRoomId, chatRoom.getStatus(), requesterNickname);
        } catch (Exception e) {
            log.warn("Failed to update status cache for room {}: {}", chatRoomId, e.getMessage());
        }

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
        if (!chatRoom.isRequestFinish()){
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

        if (approve) {      // 수락
            chatRoom.approveExit();
            message = "판결이 확정되었습니다.";
        } else {            // 거절
            chatRoom.rejectExit();
            message = "판결 요청이 거절되었습니다.";
        }

        chatRoomRepository.save(chatRoom);

        // Redis status 캐시 업데이트
        try {
            String requesterNickname = (chatRoom.getExitRequester() != null)
                    ? chatRoom.getExitRequester().getNickname()
                    : null;
            chatRoomCacheService.setStatus(chatRoomId, chatRoom.getStatus(), requesterNickname);
        } catch (Exception e) {
            log.warn("Failed to update status cache for room {}: {}", chatRoomId, e.getMessage());
        }

        // 판결 수락 시 비동기 AI 분석 및 DB 저장 트리거
        if (approve) {
            asyncJudgmentService.analyzeAndSave(chatRoomId);
        }

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
