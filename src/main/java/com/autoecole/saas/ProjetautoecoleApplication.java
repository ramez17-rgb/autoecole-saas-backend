package com.autoecole.saas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ProjetautoecoleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjetautoecoleApplication.class, args);
	}
}
