package com.pizzaria.demo.mercadoPago.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

public class WebClientConfig {

    @Configuration
    public static class RestTemplateConfig {

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();        }
    }

}
