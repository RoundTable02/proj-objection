package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.dto.common.ChatPollMessageDto;
import kuit.hackathon.proj_objection.dto.response.ChatPollResponseDto;
import kuit.hackathon.proj_objection.entity.ChatMessage;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.ChatRoomMember;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.ChatRoomMemberNotFoundException;
import kuit.hackathon.proj_objection.exception.ChatRoomNotFoundException;
import kuit.hackathon.proj_objection.repository.ChatMessageRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomMemberRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChatPollService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional(readOnly = true)
    public ChatPollResponseDto poll(Long chatRoomId, Long lastMessageId, User user) {
        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        // 사용자가 채팅방 멤버인지 확인
        chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, user)
                .orElseThrow(ChatRoomMemberNotFoundException::new);

        // lastMessageId가 null이면 0으로 처리 (첫 폴링 지원)
        Long effectiveLastMessageId = (lastMessageId != null) ? lastMessageId : 0L;

        // lastMessageId 이후 메시지 조회
        List<ChatMessage> messages = chatMessageRepository
                .findByChatRoomAndIdGreaterThanOrderByIdAsc(chatRoom, effectiveLastMessageId);

        // 메시지를 DTO로 변환
        List<ChatPollMessageDto> messageDtos = messages.stream()
                .map(msg -> new ChatPollMessageDto(
                        msg.getId(),
                        msg.getSender().getNickname(),
                        msg.getContent(),
                        msg.getCreatedAt()
                ))
                .collect(Collectors.toList());

        // 채팅방 상태
        ChatRoom.RoomStatus chatRoomStatus = chatRoom.getStatus();

        // 종료 요청자 닉네임
        String finishRequestNickname = null;
        if (chatRoom.getExitRequester() != null) {
            finishRequestNickname = chatRoom.getExitRequester().getNickname();
        }

        // percent 구성: PARTICIPANT만 필터링하여 닉네임 → percent 맵 생성
        List<ChatRoomMember> allMembers = chatRoomMemberRepository.findByChatRoom(chatRoom);
        Map<String, Integer> percent = allMembers.stream()
                .filter(member -> member.getRole() == ChatRoomMember.MemberRole.PARTICIPANT)
                .collect(Collectors.toMap(
                        member -> member.getUser().getNickname(),
                        ChatRoomMember::getPercent
                ));

        return ChatPollResponseDto.builder()
                .messages(messageDtos)
                .chatRoomStatus(chatRoomStatus)
                .finishRequestNickname(finishRequestNickname)
                .percent(percent)
                .build();
    }
}
