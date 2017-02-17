package edu.neu.android.wocketslib.activities.helpcomment;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

//see http://www.jondev.net/articles/Sending_Emails_without_User_Intervention_(no_Intents)_in_Android
//from which this is derived

public class Mail extends javax.mail.Authenticator {
	private String username = "";
	private String password = "";

	private String fromAddress = "";
	private ArrayList<String> toAddresses = new ArrayList<String>();

	private String subject = "";
	private String body = "";

	private String smtpPort = "";
	private String socketFactoryPort = "";
	private String host = "";

	private boolean useSmtpAuthentication = false;

	private Multipart multipart = new MimeMultipart();

	public Mail() {

		// There is something wrong with MailCap, javamail can not find a
		// handler for the multipart/mixed part, so this bit needs to be added.
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);
	}

	public boolean send() throws Exception {
		if ((!(getUsername().equals(""))) && (!(getPassword().equals(""))) && (getToAddresses().size() > 0)) {

			Properties properties = new Properties();

			properties.put("mail.smtp.host", getHost());

			if (usesSmtpAuthentication()) {
				properties.put("mail.smtp.auth", "true");
			}

			properties.put("mail.smtp.port", getSmtpPort());
			properties.put("mail.smtp.socketFactory.port", getSocketFactoryPort());
			properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			properties.put("mail.smtp.socketFactory.fallback", "false");

			Session session = Session.getInstance(properties, this);

			MimeMessage message = new MimeMessage(session);

			message.setFrom(new InternetAddress(getFromAddress()));

			ArrayList<InternetAddress> toInternetAddresses = new ArrayList<InternetAddress>();
			for (String toAddress : getToAddresses()) {
				toInternetAddresses.add(new InternetAddress(toAddress));
			}

			message.setRecipients(MimeMessage.RecipientType.TO, toInternetAddresses.toArray(new InternetAddress[toInternetAddresses.size()]));

			message.setSubject(getSubject());
			message.setSentDate(new Date());

			BodyPart bodyPart = new MimeBodyPart();
			bodyPart.setText(getBody());
			multipart.addBodyPart(bodyPart);

			message.setContent(multipart);

			Transport.send(message);

			return true;
		} else {
			return false;
		}
	}

	public void addAttachment(String fileName) throws Exception {
		BodyPart bodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(fileName);
		bodyPart.setDataHandler(new DataHandler(source));
		bodyPart.setFileName(fileName);

		multipart.addBodyPart(bodyPart);
	}

	@Override
	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(getUsername(), getPassword());
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setToAddresses(ArrayList<String> toAddresses) {
		this.toAddresses = toAddresses;
	}

	public ArrayList<String> getToAddresses() {
		return toAddresses;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return subject;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getBody() {
		return body;
	}

	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public void setSocketFactoryPort(String socketFactoryPort) {
		this.socketFactoryPort = socketFactoryPort;
	}

	public String getSocketFactoryPort() {
		return socketFactoryPort;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setUsesSmtpAuthentication(boolean useSmtpAuthentication) {
		this.useSmtpAuthentication = useSmtpAuthentication;
	}

	public boolean usesSmtpAuthentication() {
		return useSmtpAuthentication;
	}
}