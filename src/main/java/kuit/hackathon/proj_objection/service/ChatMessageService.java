package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.dto.ChatMessageDto;
import kuit.hackathon.proj_objection.dto.ChatMessageListDto;
import kuit.hackathon.proj_objection.entity.ChatMessage;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.ChatRoomMember;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.ChatRoomClosedException;
import kuit.hackathon.proj_objection.exception.ChatRoomMemberNotFoundException;
import kuit.hackathon.proj_objection.exception.ChatRoomNotFoundException;
import kuit.hackathon.proj_objection.exception.MessageSendPermissionDeniedException;
import kuit.hackathon.proj_objection.repository.ChatMessageRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomMemberRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 메시지 전송
    @Transactional
    public ChatMessageDto sendMessage(Long chatRoomId, User sender, String content) {
        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        if (chatRoom.getStatus() == ChatRoom.RoomStatus.DONE) {
            throw new ChatRoomClosedException();
        }

        // 메시지 전송자가 채팅방 멤버인지 확인
        ChatRoomMember senderMember = chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, sender)
                .orElseThrow(ChatRoomMemberNotFoundException::new);

        // OBSERVER는 메시지 전송 불가
        if (senderMember.getRole() == ChatRoomMember.MemberRole.OBSERVER) {
            throw new MessageSendPermissionDeniedException();
        }

        // 메시지 저장
        ChatMessage message = ChatMessage.create(chatRoom, sender, content);
        ChatMessage savedMessage = chatMessageRepository.save(message);

        ChatMessageDto messageDto = new ChatMessageDto(
                savedMessage.getId(),
                sender.getId(),
                sender.getNickname(),
                savedMessage.getContent(),
                savedMessage.getCreatedAt(),
                ChatMessageDto.MessageType.OTHER // 브로드캐스트시에는 OTHER로 설정
        );

        // WebSocket으로 브로드캐스트
        // /topic/chatroom/{chatRoomId}로 메시지 브로드캐스트
        // messagingTemplate.convertAndSend("/topic/chatroom/" + chatRoomId, messageDto);

        return messageDto;
    }

    // 채팅방의 모든 메시지 조회 (시간 역순)
    @Transactional(readOnly = true)
    public List<ChatMessageListDto> getChatMessages(Long chatRoomId, User user) {
        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        // 사용자가 채팅방 멤버인지 확인
        chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, user)
                .orElseThrow(ChatRoomMemberNotFoundException::new);

        // 메시지 조회 (시간 역순)
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(chatRoom);

        // DTO 변환 (내가 보낸 메시지는 ME, 다른 사람 메시지는 OTHER)
        return messages.stream()
                .map(msg -> new ChatMessageListDto(
                        msg.getId(),
                        msg.getSender().getNickname(),
                        msg.getContent(),
                        msg.getCreatedAt(),
                        msg.getSender().getId().equals(user.getId())
                                ? ChatMessageDto.MessageType.ME
                                : ChatMessageDto.MessageType.OTHER
                ))
                .collect(Collectors.toList());
    }

    // 채팅방의 마지막 메시지 이후의 메세지 조회 (시간 역순)
    @Transactional(readOnly = true)
    public List<ChatMessageListDto> getChatMessagesAfterLastMessage(Long chatRoomId, Long lastMessageId, User user) {
        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        // 사용자가 채팅방 멤버인지 확인
        chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, user)
                .orElseThrow(ChatRoomMemberNotFoundException::new);

        // 메시지 조회 (시간 역순)
        // TODO 마지막 messageId 이후부터 읽어옴
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomAndIdGreaterThanOrderByIdAsc(
                chatRoom, lastMessageId
        );


        // DTO 변환 (내가 보낸 메시지는 ME, 다른 사람 메시지는 OTHER)
        return messages.stream()
                .map(msg -> new ChatMessageListDto(
                        msg.getId(),
                        msg.getSender().getNickname(),
                        msg.getContent(),
                        msg.getCreatedAt(),
                        msg.getSender().getId().equals(user.getId())
                                ? ChatMessageDto.MessageType.ME
                                : ChatMessageDto.MessageType.OTHER
                ))
                .collect(Collectors.toList());
    }
}
