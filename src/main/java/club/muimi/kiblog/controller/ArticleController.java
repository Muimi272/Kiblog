package club.muimi.kiblog.controller;

import club.muimi.kiblog.dto.Result;
import club.muimi.kiblog.entity.Article;
import club.muimi.kiblog.service.ArticleService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public Result<Page<Article>> getArticleList(Pageable pageable) {
        return Result.ok(articleService.getAllArticles(pageable));
    }

    @GetMapping("/{id}")
    public Result<Article> getArticleList(@PathVariable Long id) {
        return Result.ok(articleService.getArticleById(id));
    }

    @GetMapping("/search")
    public Result<Page<Article>> searchArticleList(@RequestParam String keyword, Pageable pageable) {
        return Result.ok(articleService.search(keyword, pageable));
    }
}
