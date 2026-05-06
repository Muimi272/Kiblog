package club.muimi.kiblog;

import club.muimi.kiblog.config.AdminProperties;
import club.muimi.kiblog.config.BrandProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AdminProperties.class, BrandProperties.class})
public class KiblogApplication {

    public static void main(String[] args) {
        SpringApplication.run(KiblogApplication.class, args);
    }

}
