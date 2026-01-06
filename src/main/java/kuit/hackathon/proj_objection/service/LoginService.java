package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.LoginException;
import kuit.hackathon.proj_objection.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Long login(String nickname, String password) {
        User user = userRepository.findByNickname(nickname)
                .orElse(User.create(nickname, passwordEncoder.encode(password)));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new LoginException();
        }

        return userRepository.save(user).getId();
    }
}
