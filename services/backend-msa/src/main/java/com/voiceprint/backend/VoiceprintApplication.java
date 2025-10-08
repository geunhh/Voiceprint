package com.voiceprint.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ServletComponentScan
@SpringBootApplication
@EnableScheduling
@ComponentScan({"com.voiceprint.common.auth", "com.voiceprint.backend"})
public class VoiceprintApplication {

	public static void main(String[] args) {
		SpringApplication.run(VoiceprintApplication.class, args);
	}

}
