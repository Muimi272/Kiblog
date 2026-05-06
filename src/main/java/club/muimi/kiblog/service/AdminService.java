package club.muimi.kiblog.service;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import club.muimi.kiblog.dao.ArticleDao;
import club.muimi.kiblog.entity.Article;
import club.muimi.kiblog.security.TokenManager;

@Service
@Transactional
public class AdminService {

    private final AdminAccountService adminAccountService;
    private final TokenManager tokenManager;
    private final ArticleDao articleDao;

    public AdminService(AdminAccountService adminAccountService,
                        TokenManager tokenManager,
                        ArticleDao articleDao) {
        this.adminAccountService = adminAccountService;
        this.tokenManager = tokenManager;
        this.articleDao = articleDao;
    }

    @Transactional(readOnly = true)
    public String login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("用户名或密码不能为空");
        }
        if (!adminAccountService.authenticate(username, password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        return tokenManager.createToken();
    }

    public Article addArticle(Article article) {
        validateArticle(article);
        article.setId(null);
        return articleDao.save(article);
    }

    public Article updateArticle(Long id, Article updated) {
        Article existing = articleDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("文章不存在"));
        if (updated.getTitle() != null && !updated.getTitle().isBlank()) {
            existing.setTitle(updated.getTitle().trim());
        }
        if (updated.getContent() != null && !updated.getContent().isBlank()) {
            existing.setContent(updated.getContent().trim());
        }
        if (updated.getAuthor() != null && !updated.getAuthor().isBlank()) {
            existing.setAuthor(updated.getAuthor().trim());
        }
        if (updated.getTags() != null) {
            Set<String> cleaned = updated.getTags().stream()
                    .map(String::trim)
                    .filter(t -> !t.isEmpty())
                    .collect(Collectors.toSet());
            existing.setTags(cleaned);
        }
        return articleDao.save(existing);
    }

    public void deleteArticle(Long id) {
        if (!articleDao.existsById(id)) {
            throw new NoSuchElementException("文章不存在");
        }
        articleDao.deleteById(id);
    }

    private void validateArticle(Article article) {
        if (article.getTitle() == null || article.getTitle().isBlank()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (article.getContent() == null || article.getContent().isBlank()) {
            throw new IllegalArgumentException("内容不能为空");
        }
        if (article.getAuthor() == null || article.getAuthor().isBlank()) {
            throw new IllegalArgumentException("作者不能为空");
        }
        if (article.getTags() != null) {
            Set<String> cleaned = article.getTags().stream()
                    .map(String::trim)
                    .filter(t -> !t.isEmpty())
                    .collect(Collectors.toSet());
            article.setTags(cleaned);
        }
    }
}