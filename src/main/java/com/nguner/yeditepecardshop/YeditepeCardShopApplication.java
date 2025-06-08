package com.nguner.yeditepecardshop;

import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Log4j2
@EnableScheduling
@SpringBootApplication
public class YeditepeCardShopApplication implements CommandLineRunner{

    public static void main(String[] args) {
        SpringApplication.run(YeditepeCardShopApplication.class, args);

    }

    @Override
    public void run(String... args) throws Exception {
        log.info("[DEBUG] Check http://localhost:8080/swagger-ui.html for API documentation");
    }
}
