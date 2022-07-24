package com.etrieu00.auth.repository;

import com.etrieu00.auth.entity.AppUser;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AppUserRepository extends ReactiveCrudRepository<AppUser, Long> {
  Mono<AppUser> findByUserEmail(String userEmail);
}
