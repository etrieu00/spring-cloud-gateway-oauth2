package com.etrieu00.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

import java.security.Principal;
import java.util.UUID;

@Configuration
public class GatewayConfig {

  @Bean
  RouteLocator gateway(RouteLocatorBuilder routeLocatorBuilder,
                       RedisRateLimiter redisRateLimiter,
                       KeyResolver keyResolver) {
    return routeLocatorBuilder
      .routes()
      .route(spec -> spec
        .path("/auth/login", "/auth/signup", "/auth/refresh")
        .filters(filter -> filter.setRequestHeader("x-request-id", UUID.randomUUID().toString()))
        .uri("http://localhost:8081"))
      .route(spec -> spec
        .path("/auth/**")
        .filters(filter -> filter.setStatus(HttpStatus.FORBIDDEN))
        .uri("http://localhost:8081"))
      .route(spec -> spec
        .path("/accounts/**")
        .filters(filter -> filter
          .rewritePath("/accounts/(?<segment>.*)", "/auth/${segment}")
          .setRequestHeader("x-requester-id", getUserIdentifier())
          .requestRateLimiter(rate -> {
            rate.setRateLimiter(redisRateLimiter);
            rate.setRouteId("auth-service");
            rate.setKeyResolver(keyResolver);
          }))
        .uri("http://localhost:8081"))
      .route(spec -> spec
        .path("/catalog/**")
        .filters(filter -> filter
          .rewritePath("/catalog/(?<segment>.*)", "/product/${segment}")
          .setRequestHeader("x-requester-id", getUserIdentifier())
          .requestRateLimiter(rate -> {
            rate.setRateLimiter(redisRateLimiter);
            rate.setRouteId("product-service");
            rate.setKeyResolver(keyResolver);
          }))
        .uri("http://localhost:8082"))
      .build();
  }

  private String getUserIdentifier() {
    return ReactiveSecurityContextHolder.getContext()
      .map(SecurityContext::getAuthentication)
      .filter(Authentication::isAuthenticated)
      .map(Principal::getName)
      .defaultIfEmpty("SYSYTEM")
      .block();
  }

}
