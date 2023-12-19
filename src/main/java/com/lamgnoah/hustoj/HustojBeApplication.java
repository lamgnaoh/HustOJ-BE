package com.lamgnoah.hustoj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class HustojBeApplication {

  public static void main(String[] args) {
    SpringApplication.run(HustojBeApplication.class, args);
  }

}
