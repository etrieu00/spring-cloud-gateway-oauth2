package com.etrieu00.auth.handler;

import com.etrieu00.auth.config.SecurityConfig;
import com.etrieu00.auth.entity.AppUser;
import com.etrieu00.auth.repository.AppUserRepository;
import com.etrieu00.auth.rest.model.Login;
import com.etrieu00.auth.rest.model.SignUp;
import com.etrieu00.auth.rest.model.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static com.etrieu00.auth.rest.model.Token.TypeEnum.ACCESS;
import static com.etrieu00.auth.rest.model.Token.TypeEnum.REFRESH;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Service
public class SecurityHandler {

  private static final Logger LOGGER = LogManager.getLogger(SecurityHandler.class);

  private static final String TOKEN_TYPE_ACCESS = "ACCESS";
  private static final String TOKEN_TYPE_REFRESH = "REFRESH";
  private final SecurityConfig securityConfig;
  private final AppUserRepository appUserRepository;

  public SecurityHandler(SecurityConfig securityConfig,
                         AppUserRepository appUserRepository) {
    this.securityConfig = securityConfig;
    this.appUserRepository = appUserRepository;
  }

  public Mono<ServerResponse> shareJwks(ServerRequest serverRequest) {
    return ok().bodyValue(securityConfig.getJWKSet().toJSONObject());
  }

  public Mono<ServerResponse> generateAccessToken(ServerRequest serverRequest) {
    String token = serverRequest.queryParam("token").orElseThrow(RuntimeException::new);
    String uuid = securityConfig.parseAndGetClaimsRefresh(token).getClaim("uid").toString();
    return ok().bodyValue(new Token().type(ACCESS).token(securityConfig.generateAccessToken(new Date(), uuid)));
  }

  public Mono<ServerResponse> userLogin(ServerRequest serverRequest) {
    return serverRequest
      .bodyToMono(Login.class)
      .publishOn(Schedulers.boundedElastic())
      .flatMap(login -> appUserRepository
        .findByUserEmail(login.getUsername())
        .filter(user -> BCrypt.checkpw(login.getPassword(), user.getUserPassword())))
      .map(AppUser::getUuid)
      .map(UUID::toString)
      .flatMap(this::createRefreshResponse)
      .switchIfEmpty(Mono.defer(() -> Mono.error(RuntimeException::new)));
  }

  public Mono<ServerResponse> userSignUp(ServerRequest serverRequest) {
    return serverRequest
      .bodyToMono(SignUp.class)
      .publishOn(Schedulers.boundedElastic())
      .map(signUp -> AppUser.builder(builder -> builder
        .setUuid(UUID.randomUUID())
        .setUserRoles("BASIC")
        .setUserEmail(signUp.getUsername())
        .setUserPassword(BCrypt.hashpw(signUp.getPassword(), BCrypt.gensalt(12)))
        .setFirstName(signUp.getFirstname())
        .setLastName(signUp.getLastname())))
      .flatMap(user -> appUserRepository
        .findByUserEmail(user.getUserEmail())
        .switchIfEmpty(Mono.just(user))
        .filter(maybe -> Objects.isNull(maybe.getId())))
      .flatMap(appUserRepository::save)
      .map(AppUser::getUuid)
      .map(UUID::toString)
      .flatMap(this::createRefreshResponse)
      .switchIfEmpty(Mono.defer(() -> Mono.error(RuntimeException::new)));
  }

  private Mono<ServerResponse> createRefreshResponse(String uuid) {
    return ok().bodyValue(new Token().type(REFRESH).token(securityConfig.generateRefreshToken(new Date(), uuid)));
  }

}
