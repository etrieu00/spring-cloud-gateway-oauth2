package com.etrieu00.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

import java.security.Principal;

@Configuration
public class RateLimitConfig {

  @Bean
  KeyResolver keyResolver() {
    return exchange -> ReactiveSecurityContextHolder.getContext()
      .map(SecurityContext::getAuthentication)
      .filter(Authentication::isAuthenticated)
      .map(Principal::getName)
      .defaultIfEmpty("UNKNOWN");

  }

  @Bean
  RedisRateLimiter redisRateLimiter() {
    return new RedisRateLimiter(3, 1);
  }
}
