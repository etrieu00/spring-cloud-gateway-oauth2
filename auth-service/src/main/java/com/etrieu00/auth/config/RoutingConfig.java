package com.etrieu00.auth.config;

import com.etrieu00.auth.handler.SecurityHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class RoutingConfig {

  private final SecurityHandler securityHandler;

  public RoutingConfig(SecurityHandler securityHandler) {
    this.securityHandler = securityHandler;
  }

  @Bean
  RouterFunction<ServerResponse> authRoutes() {
    return route(POST("/auth/login"), securityHandler::userLogin)
      .andRoute(POST("/auth/signup"), securityHandler::userSignUp)
      .andRoute(GET("/auth/jwks.json"), securityHandler::shareJwks)
      .andRoute(POST("/auth/access"), securityHandler::generateAccessToken);
  }

  @Bean
  RouterFunction<ServerResponse> pingRoutes() {
    return route(GET("/auth/ping"), request -> ok().bodyValue("Pong"));
  }

}
