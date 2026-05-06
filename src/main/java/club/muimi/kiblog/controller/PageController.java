package club.muimi.kiblog.controller;

import club.muimi.kiblog.config.BrandProperties;
import club.muimi.kiblog.entity.Article;
import club.muimi.kiblog.service.ArticleService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.IntStream;

@Controller
@AllArgsConstructor
public class PageController {

    private static final int DEFAULT_PAGE_SIZE = 6;
    private final ArticleService articleService;
    private final BrandProperties brandProperties;

    @GetMapping({"/", "/index"})
    public String index(@PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "createTime", direction = Sort.Direction.DESC)
                        Pageable pageable,
                        Model model) {
        Page<Article> page = articleService.getAllArticles(pageable);
        fillListingModel(
                model,
                page,
                "/",
                "",
                brandProperties.getSiteName() + " | 个人博客",
                brandProperties.getHomeTitle(),
                brandProperties.getHomeSubtitle(),
                brandProperties.getHomeArticlesTitle(),
                brandProperties.getHomeArticlesDescription(),
                false
        );
        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword,
                         @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "createTime", direction = Sort.Direction.DESC)
                         Pageable pageable,
                         Model model) {
        String normalizedKeyword = normalizeKeyword(keyword);
        Page<Article> page = articleService.search(normalizedKeyword, pageable);
        boolean hasKeyword = !normalizedKeyword.isEmpty();

        String listTitle = hasKeyword
                ? "与“" + normalizedKeyword + "”相关的文章"
                : "全部文章";
        String listDescription = hasKeyword
                ? "共找到 " + page.getTotalElements() + " 篇匹配文章。"
                : "当前未输入关键词，下面展示全部文章。输入关键词后会按标题、正文、作者和标签筛选。";

        fillListingModel(
                model,
                page,
                "/search",
                normalizedKeyword,
                "搜索文章 - " + brandProperties.getSiteName(),
                brandProperties.getHomeTitle(),
                brandProperties.getHomeSubtitle(),
                listTitle,
                listDescription,
                true
        );
        return "index";
    }

    @GetMapping("/article/{id}")
    public String article(@PathVariable Long id, Model model) {
        try {
            Article article = articleService.getArticleById(id);
            model.addAttribute("pageTitle", article.getTitle() + " - " + brandProperties.getSiteName());
            model.addAttribute("article", article);
            model.addAttribute("articleTags", toDisplayTags(article.getTags()));
            model.addAttribute("readingMinutes", estimateReadingMinutes(article.getContent()));
            model.addAttribute("notFound", false);
            addBrandAttributes(model);
        } catch (NoSuchElementException e) {
            model.addAttribute("pageTitle", "文章不存在 - " + brandProperties.getSiteName());
            model.addAttribute("notFound", true);
            model.addAttribute("missingMessage", e.getMessage());
            addBrandAttributes(model);
        }
        return "article";
    }

    @GetMapping("/admin/login")
    public String adminLoginPage(Model model) {
        model.addAttribute("pageTitle", brandProperties.getSiteName() + " Admin Login");
        return "admin-login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboardPage(Model model) {
        model.addAttribute("pageTitle", brandProperties.getSiteName() + " Admin Dashboard");
        model.addAttribute("defaultAuthor", "admin");
        return "admin-dashboard";
    }

    private void fillListingModel(Model model,
                                  Page<Article> page,
                                  String basePath,
                                  String keyword,
                                  String pageTitle,
                                  String heroTitle,
                                  String heroSubtitle,
                                  String listTitle,
                                  String listDescription,
                                  boolean isSearchPage) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("heroTitle", heroTitle);
        model.addAttribute("heroSubtitle", heroSubtitle);
        model.addAttribute("listTitle", listTitle);
        model.addAttribute("listDescription", listDescription);
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("searchKeywordDisplay", keyword.isEmpty() ? "未输入" : keyword);
        model.addAttribute("isSearch", isSearchPage);
        model.addAttribute("hasKeyword", !keyword.isEmpty());
        model.addAttribute("articles", page.getContent());
        model.addAttribute("hasArticles", !page.isEmpty());
        model.addAttribute("totalResults", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("currentPageDisplay", page.getTotalPages() == 0 ? 0 : page.getNumber() + 1);
        model.addAttribute("hasPrevious", page.hasPrevious());
        model.addAttribute("hasNext", page.hasNext());
        model.addAttribute("showPagination", page.getTotalPages() > 1);
        model.addAttribute("previousUrl", buildPageUrl(basePath, keyword, Math.max(page.getNumber() - 1, 0), page.getSize()));
        model.addAttribute("nextUrl", buildPageUrl(basePath, keyword, page.getNumber() + 1, page.getSize()));
        model.addAttribute("pageLinks", createPageLinks(page, basePath, keyword));
        addBrandAttributes(model);
    }

    private List<PageLink> createPageLinks(Page<Article> page, String basePath, String keyword) {
        return IntStream.range(0, page.getTotalPages())
                .mapToObj(index -> new PageLink(
                        index + 1,
                        buildPageUrl(basePath, keyword, index, page.getSize()),
                        index == page.getNumber()
                ))
                .toList();
    }

    private String buildPageUrl(String basePath, String keyword, int page, int size) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(basePath)
                .queryParam("page", page)
                .queryParam("size", size);
        if (!keyword.isEmpty()) {
            builder.queryParam("keyword", keyword);
        }
        return builder.toUriString();
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private void addBrandAttributes(Model model) {
        model.addAttribute("brandSiteName", brandProperties.getSiteName());
        model.addAttribute("brandHomeArticlesTitle", brandProperties.getHomeArticlesTitle());
        model.addAttribute("brandHomeArticlesDescription", brandProperties.getHomeArticlesDescription());
        model.addAttribute("brandAboutName", brandProperties.getAboutName());
        model.addAttribute("brandAboutSubtitle", brandProperties.getAboutSubtitle());
        model.addAttribute("brandAboutDescription", brandProperties.getAboutDescription());
        model.addAttribute("brandAboutAvatarPath", resolveAvatarPath(brandProperties.getAboutAvatarPath()));
        model.addAttribute("brandAvatarFallback", buildAvatarFallback(brandProperties.getAboutName()));
    }

    private String resolveAvatarPath(String avatarPath) {
        if (avatarPath == null || avatarPath.isBlank()) {
            return "";
        }

        String normalized = avatarPath.trim().replace("\\", "/");

        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            return normalized;
        }

        if (normalized.startsWith("./")) {
            normalized = normalized.substring(2);
        }

        if (normalized.startsWith("/assets/")) {
            normalized = normalized.substring("/assets/".length());
        } else if (normalized.startsWith("assets/")) {
            normalized = normalized.substring("assets/".length());
        } else if (normalized.startsWith("/config/assets/")) {
            normalized = normalized.substring("/config/assets/".length());
        } else if (normalized.startsWith("config/assets/")) {
            normalized = normalized.substring("config/assets/".length());
        } else if (normalized.startsWith("/")) {
            return "";
        }

        java.nio.file.Path normalizedPath;
        try {
            normalizedPath = java.nio.file.Paths.get(normalized).normalize();
        } catch (RuntimeException ex) {
            return "";
        }

        String safeRelativePath = normalizedPath.toString().replace("\\", "/");
        if (safeRelativePath.isBlank()
                || normalizedPath.isAbsolute()
                || safeRelativePath.startsWith("..")
                || safeRelativePath.contains(":")) {
            return "";
        }

        return "/assets/" + safeRelativePath;
    }

    private String buildAvatarFallback(String text) {
        if (text == null || text.isBlank()) {
            return "KB";
        }

        String normalized = text.trim();
        if (normalized.length() <= 2) {
            return normalized.toUpperCase();
        }

        if (normalized.contains(" ")) {
            return Arrays.stream(normalized.split("\\s+"))
                    .filter(part -> !part.isBlank())
                    .limit(2)
                    .map(part -> part.substring(0, 1).toUpperCase())
                    .reduce("", String::concat);
        }

        return normalized.substring(0, Math.min(2, normalized.length())).toUpperCase();
    }

    private List<String> toDisplayTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(String::trim)
                .sorted(String::compareToIgnoreCase)
                .toList();
    }

    private int estimateReadingMinutes(String content) {
        if (content == null || content.isBlank()) {
            return 1;
        }
        int effectiveLength = content.replaceAll("\\s+", "").length();
        return Math.max(1, (int) Math.ceil(effectiveLength / 350.0));
    }

    private record PageLink(int number, String url, boolean current) {
    }
}
