package com.fdp.engine;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class FraudRuleEngineApplication {

	@Autowired
	private ApplicationContext applicationContext;

	public static void main(String[] args) {
		SpringApplication.run(FraudRuleEngineApplication.class, args);
	}

	//	Debug
	@PostConstruct
	public void printProfiles() {
		System.out.println(">>>> REAL ACTIVE PROFILES = " +
				String.join(", ", applicationContext.getEnvironment().getActiveProfiles()));
	}
}
