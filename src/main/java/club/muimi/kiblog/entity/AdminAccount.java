package club.muimi.kiblog.entity;

import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Data
public class AdminAccount {
    private String username;
    private String encodedPassword;

    public boolean checkPassword(String rawPassword) {
        return new BCryptPasswordEncoder().matches(rawPassword, encodedPassword);
    }

    public void updatePassword(String newRawPassword) {
        this.encodedPassword = new BCryptPasswordEncoder().encode(newRawPassword);
    }
}