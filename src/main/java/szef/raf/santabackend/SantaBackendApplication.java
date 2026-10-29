package szef.raf.santabackend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import szef.raf.santabackend.entity.Page;
import szef.raf.santabackend.repository.PageRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;

@SpringBootApplication
public class SantaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SantaBackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDefaultData(PageRepository pageRepository) {
        return args -> {
            if (pageRepository.findById("alohomora").isEmpty()) {
                PasswordEncoder encoder = new BCryptPasswordEncoder();
                Page page = new Page();
                page.setCode("alohomora");
                page.setPasswordHash(encoder.encode("alohomora"));
                page.setUpdatedAt(LocalDateTime.now());
                page.setLetterList(new ArrayList<>());
                pageRepository.save(page);
            }
        };
    }
}
