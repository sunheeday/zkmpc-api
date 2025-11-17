package com.zkrypto.zkmpc_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.zkrypto.zkmpc_api.domain")

public class ZkmpcApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZkmpcApiApplication.class, args);
	}

}
