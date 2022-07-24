package com.etrieu00.gateway;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayApplication {

  private static final Logger LOGGER = LogManager.getLogger(GatewayApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(GatewayApplication.class, args);
  }

  @Bean
  CommandLineRunner commandLineRunner() {
    return args -> {
      LOGGER.info("Application started...");
    };
  }

}