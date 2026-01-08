package kuit.hackathon.proj_objection.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${springdoc.info.title:Objection API}")
    private String title;

    @Value("${springdoc.info.description:Objection 프로젝트 API 명세서}")
    private String description;

    @Value("${springdoc.info.version:v1.0.0}")
    private String version;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version))
                .servers(List.of(
                        new Server().url("/").description("현재 서버")
                ));
    }
}
