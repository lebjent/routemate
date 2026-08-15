package com.trip.routemate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ConfigurationPropertiesScan
@EntityScan(basePackages = "com.trip.routemate")
@EnableJpaRepositories(basePackages = "com.trip.routemate")
public class RoutemateApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoutemateApplication.class, args);
	}

}
