package com.trip.routemate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RoutemateApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoutemateApplication.class, args);
	}

}
