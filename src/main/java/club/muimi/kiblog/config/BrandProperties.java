package club.muimi.kiblog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kiblog.brand")
public class BrandProperties {

    private String siteName = "Kiblog";
    private String homeTitle = "写给长期主义者的技术与生活记录";
    private String homeSubtitle = "从代码、产品和日常观察中挑出值得留存的部分，做成一份可反复翻阅的数字笔记。";
    private String homeArticlesTitle = "最新文章";
    private String homeArticlesDescription = "从最近写下的内容开始浏览，看看这段时间我在关注什么。";
    private String aboutName = "Kiblog";
    private String aboutSubtitle = "记录技术、产品与长期学习过程中的思考。";
    private String aboutDescription = "这里主要用来整理编程实践、产品体验和日常学习中的零散想法，希望把它们慢慢沉淀成更清晰、也更值得回看的内容。";
    private String aboutAvatarPath = "";
}
