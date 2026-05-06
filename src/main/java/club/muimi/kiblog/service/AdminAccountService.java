package club.muimi.kiblog.service;

import club.muimi.kiblog.config.AdminProperties;
import club.muimi.kiblog.entity.AdminAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.File;

@Slf4j
@Service
public class AdminAccountService {

    private final AdminProperties adminProperties;
    private final ObjectMapper objectMapper;
    private final Object lock = new Object();
    private AdminAccount currentAdmin;

    public AdminAccountService(AdminProperties adminProperties) {
        this.adminProperties = adminProperties;
        this.objectMapper = new ObjectMapper();
        loadOrInit();
    }

    private void loadOrInit() {
        File file = new File(adminProperties.getDataFile());
        if (file.exists()) {
            try {
                synchronized (lock) {
                    currentAdmin = objectMapper.readValue(file, AdminAccount.class);
                }
                log.info("已从 {} 加载管理员配置", file.getAbsolutePath());
            } catch (JacksonException e) {
                throw new RuntimeException("管理员配置文件损坏，无法启动", e);
            }
        } else {
            AdminAccount admin = new AdminAccount();
            admin.setUsername(adminProperties.getUsername());

            String rawPassword = adminProperties.getPassword();
            if (isBCryptHash(rawPassword)) {
                admin.setEncodedPassword(rawPassword);
            } else {
                admin.updatePassword(rawPassword);
            }

            try {
                file.getParentFile().mkdirs();
                synchronized (lock) {
                    objectMapper.writeValue(file, admin);
                    currentAdmin = admin;
                }
                log.info("管理员信息已保存至 {}", file.getAbsolutePath());
            } catch (JacksonException e) {
                throw new RuntimeException("无法保存管理员配置文件", e);
            }
        }
    }

    private boolean isBCryptHash(String password) {
        return password != null && password.matches("^\\$2[aby]\\$\\d{2}\\$.+");
    }

    public boolean authenticate(String username, String rawPassword) {
        AdminAccount admin = getCurrent();
        return admin.getUsername().equals(username) && admin.checkPassword(rawPassword);
    }


    public AdminAccount getCurrent() {
        synchronized (lock) {
            return currentAdmin;
        }
    }

    public void update(String newUsername, String newPassword) {
        synchronized (lock) {
            if (newUsername != null && !newUsername.trim().isEmpty()) {
                currentAdmin.setUsername(newUsername.trim());
            }
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                currentAdmin.updatePassword(newPassword.trim());
            }
            saveToFile();
        }
        log.info("管理员信息已更新");
    }

    private void saveToFile() {
        File file = new File(adminProperties.getDataFile());
        try {
            file.getParentFile().mkdirs();
            objectMapper.writeValue(file, currentAdmin);
        } catch (JacksonException e) {
            throw new RuntimeException("保存管理员信息失败", e);
        }
    }
}