package com.evo.bunkov.tgbotregistration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class TgbotRegistrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(TgbotRegistrationApplication.class, args);
	}

}
