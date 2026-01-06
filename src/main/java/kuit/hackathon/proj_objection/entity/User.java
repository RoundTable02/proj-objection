package kuit.hackathon.proj_objection.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String password;

    public static User create(String nickname, String password) {
        User user = new User();
        user.nickname = nickname;
        user.password = password;
        return user;
    }
}
