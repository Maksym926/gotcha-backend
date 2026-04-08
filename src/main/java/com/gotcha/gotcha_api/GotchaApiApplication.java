package com.gotcha.gotcha_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GotchaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(GotchaApiApplication.class, args);
	}

}
