package com.analyticobjects.digitalsafe.containers;

import com.analyticobjects.utility.TimeUtility;
import java.util.Date;

/**
 * A note type to store passwords.
 *
 * @author Joel Bondurant
 * @since 2013.09
 */
public class PasswordNote extends Note {

	private static final long serialVersionUID = 1L;
	private static final int DAYS_TO_EXPIRATION = 180;
	private String userName;
	private String password;
	private String url;
	private Date expirationTime;

	public PasswordNote(String title, String password) {
		super(title, "");
		this.userName = "";
		this.password = password;
		this.url = "";
		this.updateExpiration();
	}

	private void updateExpiration() {
		this.expirationTime = TimeUtility.addDays(new Date(), DAYS_TO_EXPIRATION);
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.updateTime();
		this.userName = userName;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		updateExpiration();
		this.updateTime();
		this.password = password;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.updateTime();
		this.url = url;
	}

	public Date getExpirationTime() {
		return this.expirationTime;
	}

	@Override
	public String toXML() {
		StringBuilder xmlSnip = new StringBuilder();
		xmlSnip.append("\t<PasswordNote>\n");
		xmlSnip.append("\t\t<Title>").append(this.getTitle()).append("</Title>\n");
		xmlSnip.append("\t\t<UserName>").append(this.userName).append("</UserName>\n");
		xmlSnip.append("\t\t<Password>").append(this.password).append("</Password>\n");
		xmlSnip.append("\t\t<URL>").append(this.url).append("</URL>\n");
		xmlSnip.append("\t\t<Tags>").append(this.getTagString()).append("</Tags>\n");
		xmlSnip.append("\t</PasswordNote>\n");
		return xmlSnip.toString();
	}

}
