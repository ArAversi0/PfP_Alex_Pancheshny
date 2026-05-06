package com.pfp.companion.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.pfp.companion")
@EntityScan("com.pfp.companion")
@EnableJpaRepositories("com.pfp.companion")
public class PfpCompanionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PfpCompanionApplication.class, args);
    }
}
