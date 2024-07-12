package com.solace.spring.boot.autoconfigure.springBootTests;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
public class SampleApp {

  public static void main(String[] args) {
    SpringApplication.run(SampleApp.class, args);
  }
}