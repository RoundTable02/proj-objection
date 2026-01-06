package kuit.hackathon.proj_objection.controller;

import jakarta.servlet.http.HttpSession;
import kuit.hackathon.proj_objection.dto.BaseResponse;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.UserNotFoundException;
import kuit.hackathon.proj_objection.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/test")
public class TestController {

    private final UserRepository userRepository;

    @GetMapping
    public BaseResponse<String> test() {
        return new BaseResponse<>("test successful");
    }

    @GetMapping("/me")
    public BaseResponse<String> testSession(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new UserNotFoundException("User not logged in");
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User not found with ID: " + userId)
        );
        return new BaseResponse<>(user.getNickname());
    }
}
