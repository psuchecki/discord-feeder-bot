package com.ilinx.discordfeeder.feed;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ilinx.discordfeeder.gmail.GmailEmailSender;


@Component
public class DiscordFeeder extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(DiscordFeeder.class);

	@Autowired
	private GmailEmailSender gmailEmailSender;
	@Value("${discord.token}")
	private String token;
	@Value("${alan.channel.name}")
	private String alanChannelName;
	@Value("${alan.guild.name}")
	private String alanGuildName;

	@PostConstruct
	public void connectToDiscord() throws LoginException, RateLimitedException {
		new JDABuilder(AccountType.CLIENT).setToken(token).addEventListener(this).buildAsync();
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (isAlanTradeSignal(event)) {
			try {
				String authorName = event.getAuthor().getName();
				String contentDisplay = event.getMessage().getContentDisplay();
				logger.info("New message from {}: {}", authorName, contentDisplay);
				gmailEmailSender.sendEmail(authorName + ":" + contentDisplay);
			} catch (IOException | MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean isAlanTradeSignal(MessageReceivedEvent event) {
		return event.getGuild() != null && event.getChannel()!= null && alanGuildName.equals(event.getGuild().getName())
				&& alanChannelName.equals(event.getChannel().getName());
	}

}
