package kuit.hackathon.proj_objection.controller;

import kuit.hackathon.proj_objection.annotation.LoginUser;
import kuit.hackathon.proj_objection.dto.BaseResponse;
import kuit.hackathon.proj_objection.dto.CreateChatRoomResponseDto;
import kuit.hackathon.proj_objection.dto.JoinChatRoomRequestDto;
import kuit.hackathon.proj_objection.dto.JoinChatRoomResponseDto;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/chat/room")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    // 채팅방 생성
    @PostMapping("/create")
    public BaseResponse<CreateChatRoomResponseDto> createChatRoom(@LoginUser User user) {
        CreateChatRoomResponseDto response = chatRoomService.createChatRoom(user);
        return new BaseResponse<>(response);
    }

    // 초대 코드로 채팅방 입장
    @PostMapping("/join")
    public BaseResponse<JoinChatRoomResponseDto> joinChatRoom(
            @RequestBody JoinChatRoomRequestDto request,
            @LoginUser User user
    ) {
        JoinChatRoomResponseDto response = chatRoomService.joinChatRoom(request.getInviteCode(), user);
        return new BaseResponse<>(response);
    }
}
