package club.muimi.kiblog.dao;

import club.muimi.kiblog.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleDao extends JpaRepository<Article, Long> {
    @Query("SELECT DISTINCT a FROM Article a " +
            "LEFT JOIN a.tags t " +
            "WHERE a.title LIKE CONCAT('%', :keyword, '%') " +
            "OR a.content LIKE CONCAT('%', :keyword, '%') " +
            "OR a.author LIKE CONCAT('%', :keyword, '%') " +
            "OR t = :keyword")
    List<Article> search(@Param("keyword") String keyword);
}