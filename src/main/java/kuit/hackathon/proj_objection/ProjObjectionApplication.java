package kuit.hackathon.proj_objection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ProjObjectionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjObjectionApplication.class, args);
	}

}
