package com.green.min;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MinApplication {

	public static void main(String[] args) {
		SpringApplication.run(MinApplication.class, args);
	}

}
