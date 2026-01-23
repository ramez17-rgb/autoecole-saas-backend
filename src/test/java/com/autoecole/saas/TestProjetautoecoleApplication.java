package com.autoecole.saas;

import org.springframework.boot.SpringApplication;

public class TestProjetautoecoleApplication {

	public static void main(String[] args) {
		SpringApplication.from(ProjetautoecoleApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
