package kuit.hackathon.proj_objection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "메시지 전송 요청")
@Getter
@AllArgsConstructor
public class SendMessageRequestDto {

    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String content;
}
