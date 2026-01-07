package kuit.hackathon.proj_objection.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 메시지를 받을 prefix
        config.enableSimpleBroker("/topic");

        // 클라이언트가 메시지를 보낼 prefix
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

//    // HTTP 세션을 WebSocket 세션으로 복사
//    private static class HttpSessionHandshakeInterceptor implements HandshakeInterceptor {
//        @Override
//        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                                       WebSocketHandler wsHandler, Map<String, Object> attributes) {
//            if (request instanceof ServletServerHttpRequest) {
//                ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
//                HttpSession session = servletRequest.getServletRequest().getSession(false);
//                if (session != null) {
//                    attributes.put("userId", session.getAttribute("userId"));
//                }
//            }
//            return true;
//        }
//
//        @Override
//        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                                   WebSocketHandler wsHandler, Exception exception) {
//        }
//    }
}
