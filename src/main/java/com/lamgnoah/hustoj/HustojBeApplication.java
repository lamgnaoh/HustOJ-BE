package com.lamgnoah.hustoj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableScheduling
@EnableRetry
public class HustojBeApplication {

  public static void main(String[] args) {
    SpringApplication.run(HustojBeApplication.class, args);
  }

}
