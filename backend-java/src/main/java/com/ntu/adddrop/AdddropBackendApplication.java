package com.ntu.adddrop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // for selenium swap operations
public class AdddropBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdddropBackendApplication.class, args);
	}

}
