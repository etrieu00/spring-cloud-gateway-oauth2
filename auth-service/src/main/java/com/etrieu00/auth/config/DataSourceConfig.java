package com.etrieu00.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Configuration
@EnableR2dbcAuditing
public class DataSourceConfig {

  @Bean
  public ReactiveAuditorAware<UUID> currentAuditor() {
    return () -> Mono.just(UUID.fromString("00000000-0000-0000-0000-000000000000"));
  }
}