package club.muimi.kiblog.config;

import club.muimi.kiblog.security.AdminAuthInterceptor;
import club.muimi.kiblog.security.TokenManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TokenManager tokenManager;

    public WebConfig(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuthInterceptor(tokenManager))
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login", "/admin/dashboard");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String assetLocation = Paths.get("config", "assets")
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();

        registry.addResourceHandler("/assets/**")
                .addResourceLocations(assetLocation.endsWith("/") ? assetLocation : assetLocation + "/");
    }
}
