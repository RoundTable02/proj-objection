package kuit.hackathon.proj_objection.controller;

import kuit.hackathon.proj_objection.annotation.LoginUser;
import kuit.hackathon.proj_objection.dto.BaseResponse;
import kuit.hackathon.proj_objection.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping
    public BaseResponse<String> test() {
        return new BaseResponse<>("test successful");
    }

    @GetMapping("/me")
    public BaseResponse<String> testSession(@LoginUser User user) {
        return new BaseResponse<>(user.getNickname());
    }
}
