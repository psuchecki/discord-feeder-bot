package com.ilinx.discordfeeder.gmail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

@Component
public class GmailEmailSender {
	public static final String SUBJECT = "New signal from AlanMasters";
	public static final String AUTHORIZED_USER = "me";

	@Autowired
	private Gmail gmailService;

	@Value("${email.to}")
	private String to;
	@Value("${email.from}")
	private String from;

	public void sendEmail(String emailContent) throws IOException, MessagingException {
		Message message = createEmail(to, from, SUBJECT, emailContent);
		gmailService.users().messages().send(AUTHORIZED_USER, message).execute();
	}

	public Message createEmail(String to, String from, String subject, String bodyText)
			throws MessagingException, IOException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);

		email.setFrom(new InternetAddress(from));
		email.addRecipient(RecipientType.TO, new InternetAddress(to));
		email.setSubject(subject);
		email.setText(bodyText);

		return createMessageWithEmail(email);
	}

	public Message createMessageWithEmail(MimeMessage emailContent)
			throws MessagingException, IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		emailContent.writeTo(buffer);
		byte[] bytes = buffer.toByteArray();
		String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
		Message message = new Message();
		message.setRaw(encodedEmail);
		return message;
	}

}
