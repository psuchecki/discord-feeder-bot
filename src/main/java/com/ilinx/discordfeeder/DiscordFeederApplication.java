package com.ilinx.discordfeeder;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StopWatch;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import com.ilinx.discordfeeder.gmail.GmailConfiguration;

@SpringBootApplication
public class DiscordFeederApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscordFeederApplication.class, args);
	}

	@Bean
	public static Gmail gmail() throws IOException, GeneralSecurityException {
		Credential credential = GmailConfiguration.authorize();
		return new Gmail.Builder(GmailConfiguration.HTTP_TRANSPORT, GmailConfiguration.JSON_FACTORY, credential)
				.setApplicationName(GmailConfiguration.APPLICATION_NAME)
				.build();
	}

	@Bean
	public static StopWatch stopWatch() {
		return new StopWatch();
	}
}
