package com.unicauca.front.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Crear nueva lista de converters
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        
        // 1. String converter con UTF-8 (para texto plano)
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false);
        converters.add(stringConverter);
        
        // 2. Form converter (CRÍTICO para Keycloak - application/x-www-form-urlencoded)
        FormHttpMessageConverter formConverter = new FormHttpMessageConverter();
        converters.add(formConverter);
        
        // 3. Jackson converter con UTF-8 (para JSON)
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        jacksonConverter.setDefaultCharset(StandardCharsets.UTF_8);
        converters.add(jacksonConverter);
        
        // Establecer los converters
        restTemplate.setMessageConverters(converters);
        
        return restTemplate;
    }
}