package com.marizoo.owner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter () {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);   // 내 서버가 응답을 할 때 json을 자바스크립트에서 처리할 수 있게 할지를 설정
        config.addAllowedOrigin("https://localhost:3000");   // 모든 ip에 응답을 허용함
        config.addAllowedOrigin("https://i8b208.p.ssafy.io");   // 모든 ip에 응답을 허용함
        config.addAllowedHeader("*");   // 모든 header에 응답을 허용함
        config.addAllowedMethod("*");   // 모든 crud method 용청을 허용함
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
