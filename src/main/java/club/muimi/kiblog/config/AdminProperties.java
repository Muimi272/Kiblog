package club.muimi.kiblog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kiblog.admin")
public class AdminProperties {
    private String username = "admin";
    private String password = "123456";
    private String dataFile = "config/admin.json";
}