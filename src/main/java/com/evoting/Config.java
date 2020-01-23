package com.evoting;

import com.evoting.models.Election;
import com.evoting.models.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class Config {
    @Bean
    public ConcurrentHashMap users() {
        return new ConcurrentHashMap<String, User>();
    }
    @Bean
    public ConcurrentHashMap elections() {
        return new ConcurrentHashMap<String, Election>();
    }
}
