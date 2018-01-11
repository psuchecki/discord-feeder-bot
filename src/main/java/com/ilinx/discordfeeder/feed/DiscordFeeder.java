package com.ilinx.discordfeeder.feed;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.security.auth.login.LoginException;

import com.ilinx.discordfeeder.jbot.slack.SlackWebhooks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.ilinx.discordfeeder.gmail.GmailEmailSender;


@Component
public class DiscordFeeder extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(DiscordFeeder.class);

	@Autowired
	private GmailEmailSender gmailEmailSender;
	@Autowired
	private StopWatch stopWatch;

	@Value("${discord.token}")
	private String token;
	@Value("${alan.channel.name}")
	private String alanChannelName;
	@Value("${alan.guild.name}")
	private String alanGuildName;

	@Autowired
	private SlackWebhooks slackWebhooks;

	@PostConstruct
	public void connectToDiscord() throws LoginException, RateLimitedException {
		new JDABuilder(AccountType.CLIENT).setToken(token).addEventListener(this).buildAsync();
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (isAlanTradeSignal(event)) {
			try {
				stopWatch.start();
				String authorName = event.getAuthor().getName();
				String contentDisplay = event.getMessage().getContentDisplay();
				logger.info("New message from {}: {}", authorName, contentDisplay);
				String creationTime = event.getMessage().getCreationTime().toString();
				String emailContent = String.format("[%s] %s: %s", creationTime, authorName, contentDisplay);
				String imageUrl = isNotEmpty(event.getMessage().getAttachments()) ?
						event.getMessage().getAttachments().get(0).getUrl() : null;

				gmailEmailSender.sendEmail(emailContent, creationTime, imageUrl);
				slackWebhooks.invokeSlackWebhook(emailContent, creationTime, imageUrl);
				stopWatch.stop();
				logger.info("Request handled in {} miliseconds", stopWatch.getLastTaskTimeMillis());
			} catch (IOException | MessagingException e) {
				stopWatch.stop();
				e.printStackTrace();
			}
		}
	}

	private boolean isAlanTradeSignal(MessageReceivedEvent event) {
		return event.getGuild() != null && event.getChannel() != null && alanGuildName.equals(event.getGuild()
				.getName()) && alanChannelName.equals(event.getChannel().getName());
	}

}
