package com.trip.routemate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * RouteMate 백엔드 애플리케이션의 Spring Boot 시작점이다.
 *
 * {@link ConfigurationPropertiesScan}으로 환경별 설정 값을 타입 안전한 설정 객체로 등록한다.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class RoutemateApplication {

    /** 내장 웹 서버와 Spring 애플리케이션 컨텍스트를 시작한다. */
	public static void main(String[] args) {
		SpringApplication.run(RoutemateApplication.class, args);
	}

}
