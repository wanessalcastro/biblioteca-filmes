package com.exemplo.bibliotecafilmes.config;

import com.exemplo.bibliotecafilmes.interceptor.ApiVersionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ApiVersionInterceptor())
                .addPathPatterns("/filmes/**", "/diretores/**", "/generos/**",
                        "/usuarios/**", "/avaliacoes/**", "/api-keys/**");
    }
}
