package marton.speedheart.config;

import marton.speedheart.repositories.UserDAO;
import marton.speedheart.repositories.UserJPARep;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfig {
    @Value("${user.repository}")
    String rep;

    @Bean
    UserDAO userDAO() {
        return switch (rep) {
            case "JPA" -> new UserJPARep();
            default -> throw new RuntimeException();
        };
    }
}
