package com.etrieu00.product;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class ProductApplication {

  private static final Logger LOGGER = LogManager.getLogger(ProductApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(ProductApplication.class, args);
  }

  @Bean
  CommandLineRunner commandLineRunner() {
    return args -> {
      LOGGER.info("Application started...");
    };
  }

  @Bean
  RouterFunction<ServerResponse> exampleProductRoutes() {
    return route(GET("/product/new"), req -> ok().bodyValue(Map.of(
      "NK65", "InStock",
      "Ikki68", "OutOfStock",
      "KBD67 Lite R3", "InStock",
      "Qk68", "OutOfStock"
    )));
  }

}
