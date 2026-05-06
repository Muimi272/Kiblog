package club.muimi.kiblog.service;

import club.muimi.kiblog.dao.ArticleDao;
import club.muimi.kiblog.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)   // 整个 Service 默认只读
public class ArticleService {

    private final ArticleDao articleDao;

    public ArticleService(ArticleDao articleDao) {
        this.articleDao = articleDao;
    }

    public Article getArticleById(Long id) {
        return articleDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("文章不存在"));
    }

    public Page<Article> getAllArticles(Pageable pageable) {
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "createTime");
        return listToPage(articleDao.findAll(sort), pageable);
    }

    public Page<Article> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllArticles(pageable);
        }
        String kw = keyword.trim();
        if (kw.length() > 200) {
            kw = kw.substring(0, 200);
        }
        List<Article> result = articleDao.search(kw);
        result.sort(Comparator.comparing(
                Article::getCreateTime,
                Comparator.nullsLast(Comparator.naturalOrder())
        ).reversed());
        return listToPage(result, pageable);
    }

    private Page<Article> listToPage(List<Article> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        if (start >= list.size()) {
            return Page.empty(pageable);
        }
        List<Article> pageContent = list.subList(start, end);
        return new PageImpl<>(pageContent, pageable, list.size());
    }
}
