package club.muimi.kiblog.controller;

import club.muimi.kiblog.config.BrandProperties;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class PageModelAdvice {

    private final BrandProperties brandProperties;

    public PageModelAdvice(BrandProperties brandProperties) {
        this.brandProperties = brandProperties;
    }

    @ModelAttribute
    public void addSharedAttributes(Model model) {
        model.addAttribute("brandSiteName", brandProperties.getSiteName());
    }
}
