package club.muimi.kiblog.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TokenManager {

    private final Map<String, Long> tokenStore = new ConcurrentHashMap<>();
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000L;
    private static final int MAX_TOKENS = 20;

    public String createToken() {
        if (tokenStore.size() >= MAX_TOKENS) {
            cleanExpiredTokens();
            if (tokenStore.size() >= MAX_TOKENS) {
                log.warn("Token 数量超过阈值({})且无过期 token，可能遭受攻击，已清空所有 token", MAX_TOKENS);
                tokenStore.clear();
            }
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, System.currentTimeMillis() + EXPIRATION_MS);
        return token;
    }

    public boolean isValid(String token) {
        Long expireTime = tokenStore.get(token);
        if (expireTime == null) return false;
        if (System.currentTimeMillis() > expireTime) {
            tokenStore.remove(token);
            return false;
        }
        return true;
    }

    public void removeToken(String token) {
        tokenStore.remove(token);
    }

    private void cleanExpiredTokens() {
        long now = System.currentTimeMillis();
        tokenStore.values().removeIf(expireTime -> expireTime < now);
    }
}