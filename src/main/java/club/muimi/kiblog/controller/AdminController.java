package club.muimi.kiblog.controller;

import club.muimi.kiblog.dto.Result;
import club.muimi.kiblog.entity.Article;
import club.muimi.kiblog.service.AdminAccountService;
import club.muimi.kiblog.service.AdminService;
import club.muimi.kiblog.service.ArticleService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String token = adminService.login(body.get("username"), body.get("password"));
        return Result.ok(Map.of("token", token));
    }

    @PostMapping("/add")
    public Result<Article> add(@RequestBody Article body) {
        return Result.ok(adminService.addArticle(body));
    }

    @PutMapping("/update")
    public Result<Article> update(@RequestParam Long id, @RequestBody Article body) {
        return Result.ok(adminService.updateArticle(id, body));
    }

    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        adminService.deleteArticle(id);
        return Result.ok();
    }
}
