package net.ketone.accrptgen.app.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import java.net.MalformedURLException;
import java.util.List;

@Slf4j
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/app/**")
                .addResourceLocations("/app/")
                .resourceChain(false)
                .addResolver(new ResourceResolver() {
                    @Override
                    public Resource resolveResource(HttpServletRequest request, String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {
                        try {
                            return new FileUrlResource("/index.html");
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public String resolveUrlPath(String resourcePath, List<? extends Resource> locations, ResourceResolverChain chain) {
                        return "/index.html";
                    }
                })
        ;
    }
}
