package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.dto.FinalJudgementResponseDto;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.FinalJudgement;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.ChatRoomMemberNotFoundException;
import kuit.hackathon.proj_objection.exception.ChatRoomNotFoundException;
import kuit.hackathon.proj_objection.exception.FinalJudgementNotFoundException;
import kuit.hackathon.proj_objection.repository.ChatRoomMemberRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomRepository;
import kuit.hackathon.proj_objection.repository.FinalJudgementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class FinalJudgementService {

    private final FinalJudgementRepository finalJudgementRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    /**
     * 채팅방 ID로 최종 판결문 조회
     * 채팅방 멤버만 조회 가능
     *
     * @param chatRoomId 채팅방 ID
     * @param user 요청 사용자
     * @return 최종 판결문 응답 DTO
     */
    @Transactional(readOnly = true)
    public FinalJudgementResponseDto getByChatRoomId(Long chatRoomId, User user) {
        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        // 사용자가 채팅방 멤버인지 확인
        chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, user)
                .orElseThrow(ChatRoomMemberNotFoundException::new);

        // 최종 판결문 조회
        FinalJudgement judgement = finalJudgementRepository.findByChatRoom_Id(chatRoomId)
                .orElseThrow(FinalJudgementNotFoundException::new);

        return FinalJudgementResponseDto.from(judgement);
    }
}
