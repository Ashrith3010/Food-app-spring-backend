package com.food_api.food_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.food_api.food_api.repository")
@EntityScan(basePackages = "com.food_api.food_api.entity")
@SpringBootApplication
public class FoodApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodApiApplication.class, args);
	}

}
