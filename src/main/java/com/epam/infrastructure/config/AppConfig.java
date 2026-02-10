package com.epam.infrastructure.config;

import com.epam.infrastructure.logging.TransactionIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public FilterRegistrationBean<TransactionIdFilter> txIdFilter(TransactionIdFilter filter) {
        FilterRegistrationBean<TransactionIdFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setOrder(1);
        reg.addUrlPatterns("/*");
        return reg;
    }
}