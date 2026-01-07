package kuit.hackathon.proj_objection.client.openai.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageTest {

    @Test
    @DisplayName("Message.user()로 사용자 메시지 생성")
    void user_shouldCreateUserMessage() {
        // given
        String content = "Hello, AI!";

        // when
        Message message = Message.user(content);

        // then
        assertThat(message.getRole()).isEqualTo("user");
        assertThat(message.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("Message.system()으로 시스템 메시지 생성")
    void system_shouldCreateSystemMessage() {
        // given
        String content = "You are a helpful assistant.";

        // when
        Message message = Message.system(content);

        // then
        assertThat(message.getRole()).isEqualTo("system");
        assertThat(message.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("Message.assistant()로 어시스턴트 메시지 생성")
    void assistant_shouldCreateAssistantMessage() {
        // given
        String content = "I'm here to help you.";

        // when
        Message message = Message.assistant(content);

        // then
        assertThat(message.getRole()).isEqualTo("assistant");
        assertThat(message.getContent()).isEqualTo(content);
    }
}
