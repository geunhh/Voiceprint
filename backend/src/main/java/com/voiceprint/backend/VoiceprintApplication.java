package com.voiceprint.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class VoiceprintApplication {

	public static void main(String[] args) {
		SpringApplication.run(VoiceprintApplication.class, args);
	}

}
