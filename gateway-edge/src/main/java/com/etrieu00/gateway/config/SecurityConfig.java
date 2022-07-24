package com.etrieu00.gateway.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.DefaultJWKSetCache;
import com.nimbusds.jose.jwk.source.JWKSetCache;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  private static final Logger LOGGER = LogManager.getLogger(SecurityConfig.class);

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity,
                                                       AuthenticationWebFilter authenticationWebFilter) {
    return configurePaths(httpSecurity)
      .and()
      .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
      .httpBasic().disable()
      .csrf().disable()
      .formLogin().disable()
      .logout().disable()
      .build();
  }

  private ServerHttpSecurity.AuthorizeExchangeSpec configurePaths(ServerHttpSecurity exchange) {
    return exchange
      .authorizeExchange()
      .pathMatchers("/auth/signup").permitAll()
      .pathMatchers("/auth/login").permitAll()
      .pathMatchers("/auth/refresh").permitAll()
      .pathMatchers("/**")
      .authenticated();
  }

  @Bean
  AuthenticationWebFilter customAuthenticationWebFilter(AuthenticationProvider provider) {
    var filter = new AuthenticationWebFilter(reactiveAuthenticationManager(provider));
    filter.setServerAuthenticationConverter(serverAuthenticationConverter());
    return filter;
  }

  @Bean
  ReactiveAuthenticationManager reactiveAuthenticationManager(AuthenticationProvider authenticationProvider) {
    return authentication ->
      Mono.just(authentication)
        .map(authenticationProvider::authenticate)
        .onErrorResume(error -> Mono.just(unauthenticated(null, null)));
  }

  @Bean
  AuthenticationProvider authenticationProvider() throws MalformedURLException {
    return new AuthenticationProvider() {

      final JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
      final URL keySetUrl = new URL("http://localhost:8081/auth/jwks.json");
      final JWKSetCache jwkSetCache = new DefaultJWKSetCache(120, 60, TimeUnit.MINUTES);
      final RemoteJWKSet<SecurityContext> jwkSet = new RemoteJWKSet<>(keySetUrl, null, jwkSetCache);
      final JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, jwkSet);
      final Set<String> claims = Set.of("sub", "iat", "exp", "jti", "uid");

      @Override
      public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var jwtProcessor = new DefaultJWTProcessor<>();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().issuer("https://localhost:8081").build();
        jwtProcessor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("acc+jwt")));
        jwtProcessor.setJWSKeySelector(keySelector);
        jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(claimsSet, claims));
        try {
          JWTClaimsSet jwtClaimsSet = jwtProcessor.process(authentication.getCredentials().toString(), null);
          return authenticated(jwtClaimsSet.getClaim("uid"), jwtClaimsSet, Collections.emptyList());
        } catch (ParseException | BadJOSEException | JOSEException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      }

      @Override
      public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
      }
    };
  }

  @Bean
  ServerAuthenticationConverter serverAuthenticationConverter() {
    return exchange -> Mono.justOrEmpty(exchange)
      .flatMap(data -> Mono.justOrEmpty(data.getRequest().getHeaders().get(AUTHORIZATION)))
      .filter(bearer -> !bearer.isEmpty())
      .map(token -> token.get(0).split(" ")[1])
      .map(principle -> new UsernamePasswordAuthenticationToken(principle, principle));
  }
}
